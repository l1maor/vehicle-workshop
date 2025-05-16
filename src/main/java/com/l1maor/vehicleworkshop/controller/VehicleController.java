package com.l1maor.vehicleworkshop.controller;

import com.l1maor.vehicleworkshop.dto.VehicleDto;
import com.l1maor.vehicleworkshop.dto.VehicleRegistrationDto;
import com.l1maor.vehicleworkshop.entity.BatteryType;
import com.l1maor.vehicleworkshop.entity.ConversionHistory;
import com.l1maor.vehicleworkshop.entity.DieselVehicle;
import com.l1maor.vehicleworkshop.entity.ElectricVehicle;
import com.l1maor.vehicleworkshop.entity.FuelType;
import com.l1maor.vehicleworkshop.entity.GasVehicle;
import com.l1maor.vehicleworkshop.entity.InjectionPumpType;
import com.l1maor.vehicleworkshop.entity.Vehicle;
import com.l1maor.vehicleworkshop.entity.VehicleType;
import com.l1maor.vehicleworkshop.service.SseService;
import com.l1maor.vehicleworkshop.service.ValidationService;
import com.l1maor.vehicleworkshop.service.VehicleService;
import com.l1maor.vehicleworkshop.service.DtoMapperService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import jakarta.validation.Valid;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(VehicleController.class);

    private final VehicleService vehicleService;
    private final SseService sseService;
    private final DtoMapperService dtoMapperService;
    private final ValidationService validationService;

    public VehicleController(VehicleService vehicleService, SseService sseService, 
                          DtoMapperService dtoMapperService, ValidationService validationService) {
        this.vehicleService = vehicleService;
        this.sseService = sseService;
        this.dtoMapperService = dtoMapperService;
        this.validationService = validationService;
    }

    @GetMapping
    public ResponseEntity<List<VehicleDto>> getAllVehicles() {
        logger.debug("Fetching all vehicles");
        List<Vehicle> vehicles = vehicleService.findAllVehicles();
        List<VehicleDto> dtos = vehicles.stream()
                .map(dtoMapperService::mapToDto)
                .collect(Collectors.toList());
        logger.debug("Returning {} vehicles", dtos.size());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<VehicleDto>> getAllVehiclesPaginated(
            Pageable pageable) {
        logger.debug("Fetching paginated vehicles, page: {}, size: {}, sort: {}", 
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<Vehicle> vehiclePage = vehicleService.findAllVehiclesPaginated(pageable);
        Page<VehicleDto> dtoPage = vehiclePage.map(dtoMapperService::mapToDto);
        logger.debug("Returning paginated vehicles, total: {}, page: {}/{}", 
                vehiclePage.getTotalElements(), vehiclePage.getNumber() + 1, vehiclePage.getTotalPages());
        return ResponseEntity.ok(dtoPage);
    }

    /**
     * Search for vehicles with optional type filtering and pagination
     * @param searchTerm Optional search term to filter by VIN or license plate
     * @param type Optional vehicle type filter (DIESEL, ELECTRIC, GASOLINE)
     * @param pageable Pagination and sorting parameters
     * @return Paginated list of vehicle DTOs matching the search criteria
     */
    @GetMapping("/search")
    public ResponseEntity<Page<VehicleDto>> searchVehicles(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String searchTerm,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String type,
            Pageable pageable) {
        Page<Vehicle> vehiclePage;

        logger.debug("Searching with type={}, searchTerm={}", type, searchTerm);

        if (searchTerm != null) {
            searchTerm = searchTerm.trim();
        }

        if (type != null && !type.isEmpty()) {
            try {

                VehicleType vehicleType = VehicleType.valueOf(type);
                logger.debug("Searching with type={}, searchTerm={}", vehicleType.name(), searchTerm);


                vehiclePage = vehicleService.searchVehiclesByType(searchTerm, vehicleType, pageable);
                logger.debug("Search results count: {}", vehiclePage.getTotalElements());
            } catch (IllegalArgumentException e) {

                logger.warn("Invalid vehicle type: {}", type);
                return ResponseEntity.badRequest().build();
            }
        } else {

            vehiclePage = vehicleService.searchVehicles(searchTerm, pageable);
        }

        Page<VehicleDto> dtoPage = vehiclePage.map(vehicle -> {
            VehicleDto dto = dtoMapperService.mapToDto(vehicle);
            // Let the DtoMapperService handle the battery voltage and current mapping
            if (type != null && type.equalsIgnoreCase(VehicleType.ELECTRIC.name()) && vehicle.getType() == VehicleType.ELECTRIC) {
                logger.debug("Electric vehicle {} mapped with voltage={} and current={}", 
                    dto.getId(), dto.getBatteryVoltage(), dto.getBatteryCurrent());
            }
            return dto;
        });

        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleDto> getVehicleById(@PathVariable Long id) {
        logger.debug("Fetching vehicle with id: {}", id);
        return vehicleService.findById(id)
                .map(vehicle -> {
                    logger.debug("Found vehicle: id={}, type={}", id, vehicle.getType());
                    return dtoMapperService.mapToDto(vehicle);
                })
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.warn("Vehicle not found with id: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Gets the registration information for a specific vehicle
     *
     * - Diesel vehicles: License plate + type of injection pump
     * - Electric vehicles: VIN + Voltage + Current + Battery Type
     * - Gasoline vehicles: License plate + Type of fuel used
     *
     * For convertible vehicles (electric), it also includes conversion data:
     * License plate + potential fuel types
     */
    @GetMapping("/{id}/registration")
    public ResponseEntity<VehicleRegistrationDto> getVehicleRegistration(@PathVariable Long id) {
        logger.debug("Fetching registration for vehicle with id: {}", id);
        try {
            VehicleRegistrationDto registrationDto = vehicleService.getRegistrationInfo(id);
            logger.debug("Found registration for vehicle: {}, type: {}", id, registrationDto.getType());
            return ResponseEntity.ok(registrationDto);
        } catch (EntityNotFoundException e) {
            logger.warn("Vehicle registration not found for id: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/registration")
    public ResponseEntity<List<VehicleRegistrationDto>> getAllVehicleRegistrations() {
        logger.debug("Fetching all vehicle registrations");
        List<VehicleRegistrationDto> registrations = vehicleService.getAllRegistrationInfo();
        logger.debug("Returning {} vehicle registrations", registrations.size());
        return ResponseEntity.ok(registrations);
    }

    @GetMapping("/registration/paginated")
    public ResponseEntity<Page<VehicleRegistrationDto>> getAllVehicleRegistrationsPaginated(Pageable pageable) {
        logger.debug("Fetching paginated vehicle registrations, page: {}, size: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        Page<VehicleRegistrationDto> registrations = vehicleService.getAllRegistrationInfoPaginated(pageable);
        logger.debug("Returning paginated vehicle registrations, total: {}, page: {}/{}", 
                registrations.getTotalElements(), registrations.getNumber() + 1, registrations.getTotalPages());
        return ResponseEntity.ok(registrations);
    }

    /**
     * Search for vehicle registration information with optional type filtering and pagination
     * @param searchTerm Optional search term to filter by VIN or license plate
     * @param type Optional vehicle type filter (DIESEL, ELECTRIC, GASOLINE)
     * @param pageable Pagination and sorting parameters
     * @return Paginated list of vehicle registration DTOs matching the search criteria
     */
    @GetMapping("/registration/search")
    public ResponseEntity<Page<VehicleRegistrationDto>> searchVehicleRegistrations(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String searchTerm,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String type,
            Pageable pageable) {
        Page<VehicleRegistrationDto> registrations;


        if (searchTerm != null) {
            searchTerm = searchTerm.trim();
        }

        if (type != null && !type.isEmpty()) {
            try {

                VehicleType vehicleType = VehicleType.valueOf(type);
                logger.debug("Searching registrations with type={}, searchTerm={}", vehicleType, searchTerm);


                registrations = vehicleService.searchRegistrationInfoByType(searchTerm, vehicleType, pageable);
                logger.debug("Registration search results count: {}", registrations.getTotalElements());
            } catch (IllegalArgumentException e) {

                logger.warn("Invalid vehicle type for registration search: {}", type);
                return ResponseEntity.badRequest().build();
            }
        } else {

            registrations = vehicleService.searchRegistrationInfo(searchTerm, pageable);
        }

        return ResponseEntity.ok(registrations);
    }

    @PostMapping(value = "/diesel", consumes = {MediaType.APPLICATION_JSON_VALUE, "application/json;charset=UTF-8"})
    public ResponseEntity<?> createDieselVehicle(@Valid @RequestBody VehicleDto vehicleDto) {
        try {
            logger.debug("Received diesel vehicle creation request: {}", vehicleDto);

            if (vehicleDto.getType() != VehicleType.DIESEL) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Vehicle type must be DIESEL");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            // Perform additional validation for diesel vehicle-specific fields
            Map<String, String> validationErrors = validationService.validateVehicleTypeSpecificFields(vehicleDto);
            if (!validationErrors.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Validation failed");
                errorResponse.put("errors", validationErrors);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            logger.debug("Creating diesel vehicle with VIN={}, licensePlate={}, injectionPumpType={}",
                    vehicleDto.getVin(), vehicleDto.getLicensePlate(), vehicleDto.getInjectionPumpType());

            DieselVehicle vehicle = new DieselVehicle();
            vehicle.setVin(vehicleDto.getVin());
            vehicle.setLicensePlate(vehicleDto.getLicensePlate());
            vehicle.setInjectionPumpType(InjectionPumpType.valueOf(vehicleDto.getInjectionPumpType()));

            DieselVehicle saved = vehicleService.saveDieselVehicle(vehicle);
            logger.info("Successfully created diesel vehicle with ID={}", saved.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapperService.mapToDto(saved));
        } catch (IllegalArgumentException e) {
            logger.error("Error creating diesel vehicle: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (Exception e) {
            logger.error("Unexpected error creating diesel vehicle: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "An error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping(value = "/electric", consumes = {MediaType.APPLICATION_JSON_VALUE, "application/json;charset=UTF-8"})
    public ResponseEntity<?> createElectricVehicle(@Valid @RequestBody VehicleDto vehicleDto) {
        try {
            logger.debug("Received electric vehicle creation request: {}", vehicleDto);

            if (vehicleDto.getType() != VehicleType.ELECTRIC) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Vehicle type must be ELECTRIC");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            // Perform additional validation for electric vehicle-specific fields
            Map<String, String> validationErrors = validationService.validateVehicleTypeSpecificFields(vehicleDto);
            if (!validationErrors.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Validation failed");
                errorResponse.put("errors", validationErrors);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            logger.debug("Creating electric vehicle with VIN={}, licensePlate={}, batteryType={}, voltage={}, current={}",
                    vehicleDto.getVin(), vehicleDto.getLicensePlate(), vehicleDto.getBatteryType(),
                    vehicleDto.getBatteryVoltage(), vehicleDto.getBatteryCurrent());

            ElectricVehicle vehicle = new ElectricVehicle();
            vehicle.setVin(vehicleDto.getVin());
            vehicle.setLicensePlate(vehicleDto.getLicensePlate());
            vehicle.setBatteryType(BatteryType.valueOf(vehicleDto.getBatteryType()));
            vehicle.setBatteryVoltage(vehicleDto.getBatteryVoltage());
            vehicle.setBatteryCurrent(vehicleDto.getBatteryCurrent());

            if (vehicleDto.isConvertible() != null) {
                vehicle.setConvertible(vehicleDto.isConvertible());
            } else {
                vehicle.setConvertible(true);
            }

            ElectricVehicle saved = vehicleService.saveElectricVehicle(vehicle);
            logger.info("Successfully created electric vehicle with ID={}", saved.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapperService.mapToDto(saved));
        } catch (IllegalArgumentException e) {
            logger.error("Error creating electric vehicle: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (Exception e) {
            logger.error("Unexpected error creating electric vehicle: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "An error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping(value = "/gas", consumes = {MediaType.APPLICATION_JSON_VALUE, "application/json;charset=UTF-8"})
    public ResponseEntity<?> createGasVehicle(@Valid @RequestBody VehicleDto vehicleDto) {
        try {
            logger.debug("Received gasoline vehicle creation request: {}", vehicleDto);

            if (vehicleDto.getType() != VehicleType.GASOLINE) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Vehicle type must be GASOLINE");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            // Perform additional validation for gas vehicle-specific fields
            Map<String, String> validationErrors = validationService.validateVehicleTypeSpecificFields(vehicleDto);
            if (!validationErrors.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Validation failed");
                errorResponse.put("errors", validationErrors);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            logger.debug("Creating gas vehicle with VIN={}, licensePlate={}, fuelTypes={}",
                    vehicleDto.getVin(), vehicleDto.getLicensePlate(),
                    vehicleDto.getFuelTypes() != null ? Arrays.toString(vehicleDto.getFuelTypes()) : "null");

            GasVehicle vehicle = new GasVehicle();
            vehicle.setVin(vehicleDto.getVin());
            vehicle.setLicensePlate(vehicleDto.getLicensePlate());

            if (vehicleDto.getFuelTypes() != null) {
                Set<FuelType> fuelTypes = Arrays.stream(vehicleDto.getFuelTypes())
                        .map(FuelType::valueOf)
                        .collect(Collectors.toSet());
                vehicle.setFuelTypes(fuelTypes);
            }

            GasVehicle saved = vehicleService.saveGasVehicle(vehicle);
            logger.info("Successfully created gas vehicle with ID={}", saved.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapperService.mapToDto(saved));
        } catch (IllegalArgumentException e) {
            logger.error("Error creating gas vehicle: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (Exception e) {
            logger.error("Unexpected error creating gas vehicle: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "An error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateVehicle(@PathVariable Long id, @Valid @RequestBody VehicleDto vehicleDto) {
        logger.debug("Updating vehicle with id: {}", id);
        try {
            // Perform type-specific validation
            logger.debug("Validating vehicle-specific fields for type: {}", vehicleDto.getType());
            Map<String, String> validationErrors = validationService.validateVehicleTypeSpecificFields(vehicleDto);
            if (!validationErrors.isEmpty()) {
                logger.warn("Validation failed for vehicle update: {}", validationErrors);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Validation failed");
                errorResponse.put("errors", validationErrors);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            Vehicle existingVehicle = vehicleService.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + id));

            existingVehicle.setVin(vehicleDto.getVin());
            existingVehicle.setLicensePlate(vehicleDto.getLicensePlate());

            if (existingVehicle instanceof DieselVehicle && vehicleDto.getInjectionPumpType() != null) {
                ((DieselVehicle) existingVehicle).setInjectionPumpType(
                        InjectionPumpType.valueOf(vehicleDto.getInjectionPumpType()));
            } else if (existingVehicle instanceof ElectricVehicle) {
                ElectricVehicle ev = (ElectricVehicle) existingVehicle;
                if (vehicleDto.getBatteryType() != null) {
                    ev.setBatteryType(BatteryType.valueOf(vehicleDto.getBatteryType()));
                }
                if (vehicleDto.getBatteryVoltage() != null) {
                    ev.setBatteryVoltage(vehicleDto.getBatteryVoltage());
                }
                if (vehicleDto.getBatteryCurrent() != null) {
                    ev.setBatteryCurrent(vehicleDto.getBatteryCurrent());
                }
                if (vehicleDto.isConvertible() != null) {
                    ev.setConvertible(vehicleDto.isConvertible());
                }
            } else if (existingVehicle instanceof GasVehicle && vehicleDto.getFuelTypes() != null) {
                Set<FuelType> fuelTypes = Arrays.stream(vehicleDto.getFuelTypes())
                        .map(FuelType::valueOf)
                        .collect(Collectors.toSet());
                ((GasVehicle) existingVehicle).setFuelTypes(fuelTypes);
            }

            logger.debug("Saving updated vehicle with id: {}", id);
            Vehicle updated = vehicleService.saveVehicle(existingVehicle);
            logger.info("Successfully updated vehicle with id: {}, type: {}", id, updated.getType());
            return ResponseEntity.ok(dtoMapperService.mapToDto(updated));
        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgumentException updating vehicle {}: {}", id, e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (EntityNotFoundException e) {
            logger.warn("Vehicle not found for update with id: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        logger.debug("Attempting to delete vehicle with id: {}", id);
        if (vehicleService.deleteVehicle(id)) {
            logger.info("Successfully deleted vehicle with id: {}", id);
            return ResponseEntity.noContent().build();
        }
        logger.warn("Vehicle not found for deletion with id: {}", id);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<VehicleDto>> getVehiclesByType(@PathVariable String type) {
        logger.debug("Fetching vehicles by type: {}", type);
        try {
            VehicleType vehicleType = VehicleType.valueOf(type.toUpperCase());
            List<Vehicle> vehicles = vehicleService.findByType(vehicleType);
            List<VehicleDto> dtos = vehicles.stream()
                    .map(dtoMapperService::mapToDto)
                    .collect(Collectors.toList());
            logger.debug("Found {} vehicles of type {}", dtos.size(), type.toUpperCase());
            return ResponseEntity.ok(dtos);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid vehicle type requested: {}", type);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/type/{type}/paginated")
    public ResponseEntity<Page<VehicleDto>> getVehiclesByTypePaginated(
            @PathVariable String type, Pageable pageable) {
        try {
            VehicleType vehicleType = VehicleType.valueOf(type.toUpperCase());
            Page<Vehicle> vehiclePage = vehicleService.findByTypePaginated(vehicleType, pageable);
            Page<VehicleDto> dtoPage = vehiclePage.map(dtoMapperService::mapToDto);
            
            // The DtoMapperService already properly maps battery voltage and current from the database
            
            return ResponseEntity.ok(dtoPage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/is-convertible")
    public ResponseEntity<Boolean> isVehicleConvertible(@PathVariable Long id) {
        logger.debug("Checking if vehicle with id: {} is convertible", id);
        try {
            boolean convertible = vehicleService.isVehicleConvertible(id);
            logger.debug("Vehicle with id: {} is convertible: {}", id, convertible);
            return ResponseEntity.ok(convertible);
        } catch (EntityNotFoundException e) {
            logger.warn("Vehicle not found when checking convertibility with id: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/convert-to-gas")
    public ResponseEntity<?> convertElectricToGas(
            @PathVariable Long id, @Valid @RequestBody String[] fuelTypeNames) {
        try {
            if (fuelTypeNames == null || fuelTypeNames.length == 0) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "At least one fuel type must be specified");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            Set<FuelType> fuelTypes = Arrays.stream(fuelTypeNames)
                    .map(FuelType::valueOf)
                    .collect(Collectors.toSet());

            GasVehicle converted = vehicleService.convertElectricToGas(id, fuelTypes);
            return ResponseEntity.ok(dtoMapperService.mapToDto(converted));
        } catch (ObjectOptimisticLockingFailureException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "The vehicle was modified by another user. Please refresh and try again.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (IllegalStateException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamVehicleEvents() {
        logger.debug("Creating new SSE emitter for vehicle events");
        SseEmitter emitter = sseService.createEmitter();
        logger.debug("SSE emitter created successfully");
        return emitter;
    }

    /**
     * Retrieves the conversion history for a specific vehicle
     * @param id The ID of the vehicle to retrieve conversion history for
     * @return List of conversion history entries or 404 if the vehicle is not found
     */
    @GetMapping("/{id}/conversion-history")
    public ResponseEntity<?> getConversionHistory(@PathVariable Long id) {
        logger.debug("Fetching conversion history for vehicle with id: {}", id);
        try {
            List<ConversionHistory> conversionHistory = vehicleService.getConversionHistoryForVehicle(id);
            logger.debug("Found {} conversion history entries for vehicle: {}", conversionHistory.size(), id);
            return ResponseEntity.ok(conversionHistory);
        } catch (EntityNotFoundException e) {
            logger.warn("Vehicle not found when fetching conversion history: {}", id);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}
