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
import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.l1maor.vehicleworkshop.entity.VehicleRegistrationInfo;
import com.l1maor.vehicleworkshop.repository.VehicleRegistrationInfoRepository;
import com.l1maor.vehicleworkshop.event.RegistrationInfoAccessEvent;
import com.l1maor.vehicleworkshop.event.VehicleQueryEvent;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VehicleServiceImpl implements VehicleService {

    private static final Logger logger = LoggerFactory.getLogger(VehicleServiceImpl.class);

    private final VehicleRepository vehicleRepository;
    private final ConversionHistoryRepository conversionHistoryRepository;
    private final VehicleRegistrationInfoRepository registrationInfoRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SseService sseService;
    private final jakarta.persistence.EntityManager entityManager;
    private final DtoMapperService dtoMapperService;

    public VehicleServiceImpl(VehicleRepository vehicleRepository,
                            ConversionHistoryRepository conversionHistoryRepository,
                            VehicleRegistrationInfoRepository registrationInfoRepository,
                            ApplicationEventPublisher eventPublisher,
                            SseService sseService,
                            jakarta.persistence.EntityManager entityManager,
                            DtoMapperService dtoMapperService) {
        this.vehicleRepository = vehicleRepository;
        this.conversionHistoryRepository = conversionHistoryRepository;
        this.registrationInfoRepository = registrationInfoRepository;
        this.eventPublisher = eventPublisher;
        this.sseService = sseService;
        this.entityManager = entityManager;
        this.dtoMapperService = dtoMapperService;
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
    public Page<Vehicle> findAllVehiclesPaginated(Pageable pageable) {
        return vehicleRepository.findAll(pageable);
    }

    @Override
    public Page<Vehicle> findByTypePaginated(VehicleType type, Pageable pageable) {
        return vehicleRepository.findByType(type, pageable);
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

        if (!vehicle.isConvertible()) {
            throw new IllegalStateException("This electric vehicle is not convertible to gas");
        }

        if (newFuelTypes == null || newFuelTypes.isEmpty()) {
            throw new IllegalArgumentException("Fuel types cannot be empty");
        }

        // Calculate the fuel type flags by setting the flags
        int fuelTypesFlags = 0;
        for (FuelType type : newFuelTypes) {
            switch(type) {
                case B83: fuelTypesFlags |= 1; break;  // FUEL_B83 = 1
                case B90: fuelTypesFlags |= 2; break;  // FUEL_B90 = 2
                case B94: fuelTypesFlags |= 4; break;  // FUEL_B94 = 4
                case B100: fuelTypesFlags |= 8; break; // FUEL_B100 = 8
            }
        }

        // Use a direct native SQL update to change the vehicle type and attributes
        // This ensures we trigger the conversion tracking database trigger
        Query updateQuery = entityManager.createNativeQuery(
                "UPDATE vehicles SET type = :newType, "
                + "battery_type = NULL, battery_voltage = NULL, battery_current = NULL, "
                + "fuel_types_flags = :fuelFlags, "
                + "convertible = :convertible "
                + "WHERE id = :id");

        updateQuery.setParameter("newType", VehicleType.GASOLINE.name());
        updateQuery.setParameter("fuelFlags", fuelTypesFlags);
        updateQuery.setParameter("convertible", false);
        updateQuery.setParameter("id", vehicleId);
        updateQuery.executeUpdate();

        entityManager.flush();
        entityManager.clear();

        GasVehicle convertedVehicle = (GasVehicle) vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found after conversion with id: " + vehicleId));

        sseService.broadcastVehicleUpdate(convertedVehicle);

        return convertedVehicle;
    }


    @Override
    public boolean isVehicleConvertible(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .map(this::isVehicleConvertible)
                .orElse(false);
    }

    @Override
    public boolean isVehicleConvertible(Vehicle vehicle) {
        return vehicle.isConvertible();
    }

    @Override
    public List<ConversionHistory> getConversionHistoryForVehicle(Long vehicleId) {

        if (!vehicleRepository.existsById(vehicleId)) {
            throw new EntityNotFoundException("Vehicle not found with id: " + vehicleId);
        }


        return conversionHistoryRepository.findByVehicleIdOrderByConversionDateDesc(vehicleId);
    }

    @Override
    public VehicleRegistrationDto getRegistrationInfo(Long vehicleId) {

        Optional<VehicleRegistrationInfo> viewInfo = registrationInfoRepository.findById(vehicleId);
        if (viewInfo.isPresent()) {
            return dtoMapperService.mapToDto(viewInfo.get());
        }


        return vehicleRepository.findById(vehicleId)
            .map(this::createSimplifiedRegistrationDto)
            .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + vehicleId));
    }

    @Override
    public Page<VehicleRegistrationDto> getAllRegistrationInfoPaginated(Pageable pageable) {

        Page<VehicleRegistrationInfo> regInfoPage = registrationInfoRepository.findAll(pageable);
        Page<VehicleRegistrationDto> dtoPage = regInfoPage.map(dtoMapperService::mapToDto);


        eventPublisher.publishEvent(new RegistrationInfoAccessEvent("Retrieved all registrations, page " + pageable.getPageNumber()));

        return dtoPage;
    }

    @Override
    public List<VehicleRegistrationDto> getAllRegistrationInfo() {

        List<VehicleRegistrationInfo> registrations = registrationInfoRepository.findAll();
        return registrations.stream()
            .map(dtoMapperService::mapToDto)
            .collect(Collectors.toList());
    }

    /**
     * Searches for vehicles by VIN or license plate containing the search term, with pagination
     * If search term is empty or null, returns all vehicles
     *
     * @param searchTerm The search term to match against VIN or license plate
     * @param pageable Pagination and sorting parameters
     * @return Page of vehicles matching the search criteria
     */
    @Override
    public Page<Vehicle> searchVehicles(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            logger.debug("Empty search term, returning all vehicles");
            return findAllVehiclesPaginated(pageable);
        }

        String trimmedSearch = searchTerm.trim();
        logger.debug("Searching vehicles with term: {}", trimmedSearch);
        return vehicleRepository.findByVinOrLicensePlateContaining(trimmedSearch, pageable);
    }

    @Override
    public Page<Vehicle> searchVehiclesByType(String searchTerm, VehicleType type, Pageable pageable) {
        if (type == null) {
            logger.debug("Type is null, using basic search");
            return searchVehicles(searchTerm, pageable);
        }

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            logger.debug("Search term is empty, using type filter only");
            return findByTypePaginated(type, pageable);
        }


        String trimmedSearch = searchTerm.trim();
        String typeStr = type.name();
        logger.debug("Searching with term='{}', type='{}'", trimmedSearch, typeStr);


        Page<Vehicle> result = vehicleRepository.findByVinOrLicensePlateContainingAndType(trimmedSearch, typeStr, pageable);
        logger.debug("Search returned {} results", result.getTotalElements());

        return result;
    }

    /**
     * Searches for vehicle registration information by VIN or license plate
     * If search term is empty or null, returns all registrations
     *
     * @param searchTerm The search term to match against VIN or license plate
     * @param pageable Pagination and sorting parameters
     * @return Page of vehicle registration DTOs matching the search criteria
     */
    @Override
    public Page<VehicleRegistrationDto> searchRegistrationInfo(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            logger.debug("Empty registration search term, returning all registrations");
            return getAllRegistrationInfoPaginated(pageable);
        }

        String trimmedSearch = searchTerm.trim();
        logger.debug("Searching registration info with term: {}", trimmedSearch);


        Page<Vehicle> vehicles = vehicleRepository.findByVinOrLicensePlateContaining(trimmedSearch, pageable);
        logger.debug("Found {} matching vehicles for registration info", vehicles.getTotalElements());


        return vehicles.map(this::createSimplifiedRegistrationDto);
    }

    /**
     * Searches for vehicle registration information by VIN or license plate,
     * filtered by vehicle type with pagination
     *
     * @param searchTerm The search term to match against VIN or license plate
     * @param type The vehicle type to filter by
     * @param pageable Pagination and sorting parameters
     * @return Page of vehicle registration DTOs matching the search criteria and type
     */
    @Override
    public Page<VehicleRegistrationDto> searchRegistrationInfoByType(String searchTerm, VehicleType type, Pageable pageable) {
        if (type == null) {
            logger.debug("Type is null for registration search, using basic search");
            return searchRegistrationInfo(searchTerm, pageable);
        }

        if (searchTerm == null || searchTerm.trim().isEmpty()) {

            logger.debug("Empty search term for registration, filtering by type: {}", type);
            Page<Vehicle> vehicles = vehicleRepository.findByType(type, pageable);
            return vehicles.map(this::createSimplifiedRegistrationDto);
        }


        String trimmedSearch = searchTerm.trim();
        logger.debug("Searching registration info with term: {} and type: {}", trimmedSearch, type);
        Page<Vehicle> vehicles = vehicleRepository.findByVinOrLicensePlateContainingAndType(trimmedSearch, type.name(), pageable);
        logger.debug("Found {} matching vehicles for registration with type filter", vehicles.getTotalElements());

        return vehicles.map(this::createSimplifiedRegistrationDto);
    }

    /**
     * Creates a simplified VehicleRegistrationDto from a Vehicle entity
     * Used when we can't rely on the database view
     */
    private VehicleRegistrationDto createSimplifiedRegistrationDto(Vehicle vehicle) {
        VehicleRegistrationDto dto = new VehicleRegistrationDto();
        dto.setId(vehicle.getId());
        dto.setType(vehicle.getType());
        dto.setConvertible(vehicle.isConvertible());


        dto.setRegistrationInfo(vehicle.getVin() + " / " + vehicle.getLicensePlate());


        long conversionCount = conversionHistoryRepository.countByVehicleId(vehicle.getId());
        dto.setHasConversionHistory(conversionCount > 0);


        eventPublisher.publishEvent(new VehicleQueryEvent("Registration info requested for vehicle " + vehicle.getId()));

        return dto;
    }
}
