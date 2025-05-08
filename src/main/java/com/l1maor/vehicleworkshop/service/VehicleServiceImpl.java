package com.l1maor.vehicleworkshop.service;

import com.l1maor.vehicleworkshop.entity.BatteryType;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final ConversionHistoryRepository conversionHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SseService sseService;

    public VehicleServiceImpl(VehicleRepository vehicleRepository, 
                            ConversionHistoryRepository conversionHistoryRepository,
                            ApplicationEventPublisher eventPublisher,
                            SseService sseService) {
        this.vehicleRepository = vehicleRepository;
        this.conversionHistoryRepository = conversionHistoryRepository;
        this.eventPublisher = eventPublisher;
        this.sseService = sseService;
    }

    @Override
    @Transactional
    public Vehicle saveVehicle(Vehicle vehicle) {
        try {
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

        ConversionHistory history = new ConversionHistory();
        history.setVehicle(vehicle);
        history.setOriginalBatteryType(electricVehicle.getBatteryType());
        history.setOriginalVoltage(electricVehicle.getBatteryVoltage());
        history.setOriginalCurrent(electricVehicle.getBatteryCurrent());
        conversionHistoryRepository.save(history);

        GasVehicle gasVehicle = new GasVehicle();
        gasVehicle.setId(vehicle.getId());
        gasVehicle.setVin(vehicle.getVin());
        gasVehicle.setLicensePlate(vehicle.getLicensePlate());
        gasVehicle.setFuelTypes(newFuelTypes);

        GasVehicle saved = (GasVehicle) vehicleRepository.save(gasVehicle);

        sseService.broadcastVehicleUpdate(saved);
        
        return saved;
    }
}
