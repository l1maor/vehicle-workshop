package com.l1maor.vehicleworkshop.service;

import com.l1maor.vehicleworkshop.dto.VehicleRegistrationDto;
import com.l1maor.vehicleworkshop.entity.ConversionHistory;
import com.l1maor.vehicleworkshop.entity.DieselVehicle;
import com.l1maor.vehicleworkshop.entity.ElectricVehicle;
import com.l1maor.vehicleworkshop.entity.FuelType;
import com.l1maor.vehicleworkshop.entity.GasVehicle;
import com.l1maor.vehicleworkshop.entity.Vehicle;
import com.l1maor.vehicleworkshop.entity.VehicleType;
import com.l1maor.vehicleworkshop.repository.ConversionHistoryRepository;
import com.l1maor.vehicleworkshop.repository.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.EnumSet;

@Service
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final ConversionHistoryRepository conversionHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SseService sseService;
    private final jakarta.persistence.EntityManager entityManager;

    public VehicleServiceImpl(VehicleRepository vehicleRepository, 
                            ConversionHistoryRepository conversionHistoryRepository,
                            ApplicationEventPublisher eventPublisher,
                            SseService sseService,
                            jakarta.persistence.EntityManager entityManager) {
        this.vehicleRepository = vehicleRepository;
        this.conversionHistoryRepository = conversionHistoryRepository;
        this.eventPublisher = eventPublisher;
        this.sseService = sseService;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public Vehicle saveVehicle(Vehicle vehicle) {
        try {
            if (vehicle.getId() == null) {
                if (vehicleRepository.existsByVin(vehicle.getVin())) {
                    throw new IllegalArgumentException("Vehicle with VIN " + vehicle.getVin() + " already exists");
                }
                
                if (vehicleRepository.existsByLicensePlate(vehicle.getLicensePlate())) {
                    throw new IllegalArgumentException("Vehicle with license plate " + vehicle.getLicensePlate() + " already exists");
                }
            }
            
            Vehicle saved = vehicleRepository.save(vehicle);
            sseService.broadcastVehicleUpdate(saved);
            return saved;
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Vehicle with the same VIN or license plate already exists", e);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new IllegalStateException("Vehicle was updated by another user. Please refresh and try again", e);
        }
    }

    @Override
    public Optional<Vehicle> findById(Long id) {
        return vehicleRepository.findById(id);
    }

    @Override
    public Optional<Vehicle> findByVin(String vin) {
        return vehicleRepository.findByVin(vin);
    }

    @Override
    public Optional<Vehicle> findByLicensePlate(String licensePlate) {
        return vehicleRepository.findByLicensePlate(licensePlate);
    }

    @Override
    public List<Vehicle> findAllVehicles() {
        return vehicleRepository.findAll();
    }

    @Override
    public List<Vehicle> findByType(VehicleType type) {
        return vehicleRepository.findByType(type);
    }
    
    @Override
    public boolean existsByVin(String vin) {
        return vehicleRepository.existsByVin(vin);
    }
    
    @Override
    public boolean existsByLicensePlate(String licensePlate) {
        return vehicleRepository.existsByLicensePlate(licensePlate);
    }

    @Override
    @Transactional
    public boolean deleteVehicle(Long id) {
        if (vehicleRepository.existsById(id)) {
            Vehicle vehicle = vehicleRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + id));
            
            vehicleRepository.delete(vehicle);

            sseService.broadcastVehicleDelete(id);
            
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public DieselVehicle saveDieselVehicle(DieselVehicle vehicle) {
        return (DieselVehicle) saveVehicle(vehicle);
    }

    @Override
    @Transactional
    public ElectricVehicle saveElectricVehicle(ElectricVehicle vehicle) {
        return (ElectricVehicle) saveVehicle(vehicle);
    }

    @Override
    @Transactional
    public GasVehicle saveGasVehicle(GasVehicle vehicle) {
        return (GasVehicle) saveVehicle(vehicle);
    }

    @Override
    @Transactional
    public GasVehicle convertElectricToGas(Long vehicleId, Set<FuelType> newFuelTypes) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + vehicleId));
        
        if (vehicle.getType() != VehicleType.ELECTRIC) {
            throw new IllegalStateException("Only electric vehicles can be converted to gas");
        }
        
        ElectricVehicle electricVehicle = (ElectricVehicle) vehicle;

        // Save conversion history
        ConversionHistory history = new ConversionHistory();
        history.setVehicle(vehicle);
        history.setOriginalBatteryType(electricVehicle.getBatteryType());
        history.setOriginalVoltage(electricVehicle.getBatteryVoltage());
        history.setOriginalCurrent(electricVehicle.getBatteryCurrent());
        conversionHistoryRepository.save(history);
        
        // Check fuel types
        if (newFuelTypes == null || newFuelTypes.isEmpty()) {
            throw new IllegalArgumentException("Fuel types cannot be empty");
        }
        
        // Use direct SQL update to change the discriminator column
        // This will force an UPDATE instead of INSERT+DELETE
        entityManager.createNativeQuery("UPDATE vehicles SET type = 'GASOLINE', battery_type = NULL, " +
                        "battery_voltage = NULL, battery_current = NULL " +
                        "WHERE id = :id")
            .setParameter("id", vehicleId)
            .executeUpdate();
        
        // Clear persistence context to avoid stale data
        entityManager.flush();
        entityManager.clear();
        
        // Re-fetch the vehicle, now as a GasVehicle
        GasVehicle gasVehicle = (GasVehicle) vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found after conversion: " + vehicleId));
        
        // Set the fuel types
        gasVehicle.setFuelTypes(newFuelTypes);
        gasVehicle = (GasVehicle) vehicleRepository.save(gasVehicle);
        
        sseService.broadcastVehicleUpdate(gasVehicle);
        
        return gasVehicle;
    }
    
    @Override
    public boolean isVehicleConvertible(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .map(this::isVehicleConvertible)
                .orElse(false);
    }
    
    @Override
    public boolean isVehicleConvertible(Vehicle vehicle) {
        return vehicle instanceof ElectricVehicle;
    }
    
    @Override
    public VehicleRegistrationDto getRegistrationInfo(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + vehicleId));
                
        return createRegistrationInfo(vehicle);
    }
    
    @Override
    public List<VehicleRegistrationDto> getAllRegistrationInfo() {
        return vehicleRepository.findAll().stream()
                .map(this::createRegistrationInfo)
                .collect(Collectors.toList());
    }

    private VehicleRegistrationDto createRegistrationInfo(Vehicle vehicle) {
        VehicleRegistrationDto dto = new VehicleRegistrationDto();
        dto.setId(vehicle.getId());
        
        boolean isConvertible = isVehicleConvertible(vehicle);
        dto.setConvertible(isConvertible);
    
        String registrationInfo;
        VehicleType vehicleType;
        
        if (vehicle instanceof DieselVehicle) {
            DieselVehicle dieselVehicle = (DieselVehicle) vehicle;
            vehicleType = VehicleType.DIESEL;
            registrationInfo = dieselVehicle.getLicensePlate() + " + " + 
                    (dieselVehicle.getInjectionPumpType() != null ? 
                     dieselVehicle.getInjectionPumpType().name() : "UNKNOWN");
        } else if (vehicle instanceof ElectricVehicle) {
            ElectricVehicle electricVehicle = (ElectricVehicle) vehicle;
            vehicleType = VehicleType.ELECTRIC;
            registrationInfo = electricVehicle.getVin() + " + " +
                    electricVehicle.getBatteryVoltage() + "V + " +
                    electricVehicle.getBatteryCurrent() + "A + " +
                    (electricVehicle.getBatteryType() != null ? 
                     electricVehicle.getBatteryType().name() : "UNKNOWN");
    
            dto.setConversionData(electricVehicle.getLicensePlate() + " + POTENTIAL FUELS: " +
                    String.join(", ", Arrays.stream(FuelType.values())
                            .map(Enum::name)
                            .collect(Collectors.toList())));
        } else if (vehicle instanceof GasVehicle) {
            GasVehicle gasVehicle = (GasVehicle) vehicle;
            vehicleType = VehicleType.GASOLINE;
            if (gasVehicle.getFuelTypes() != null && !gasVehicle.getFuelTypes().isEmpty()) {
                registrationInfo = gasVehicle.getLicensePlate() + " + FUELS: " +
                        gasVehicle.getFuelTypes().stream()
                                .map(Enum::name)
                                .collect(Collectors.joining(", "));
            } else {
                registrationInfo = gasVehicle.getLicensePlate() + " + FUELS: UNKNOWN";
            }
        } else {
            vehicleType = vehicle.getType() != null ? vehicle.getType() : VehicleType.DIESEL; // Default to DIESEL as fallback
            registrationInfo = "Unknown vehicle type";
        }
        
        dto.setType(vehicleType);
        dto.setRegistrationInfo(registrationInfo);
        return dto;
    }
}
