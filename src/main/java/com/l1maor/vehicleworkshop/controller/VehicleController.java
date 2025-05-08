package com.l1maor.vehicleworkshop.controller;

import com.l1maor.vehicleworkshop.dto.VehicleDto;
import com.l1maor.vehicleworkshop.dto.VehicleRegistrationDto;
import com.l1maor.vehicleworkshop.entity.BatteryType;
import com.l1maor.vehicleworkshop.entity.DieselVehicle;
import com.l1maor.vehicleworkshop.entity.ElectricVehicle;
import com.l1maor.vehicleworkshop.entity.FuelType;
import com.l1maor.vehicleworkshop.entity.GasVehicle;
import com.l1maor.vehicleworkshop.entity.InjectionPumpType;
import com.l1maor.vehicleworkshop.entity.Vehicle;
import com.l1maor.vehicleworkshop.entity.VehicleType;
import com.l1maor.vehicleworkshop.service.SseService;
import com.l1maor.vehicleworkshop.service.VehicleService;
import jakarta.persistence.EntityNotFoundException;
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;
    private final SseService sseService;

    public VehicleController(VehicleService vehicleService, SseService sseService) {
        this.vehicleService = vehicleService;
        this.sseService = sseService;
    }

    @GetMapping
    public ResponseEntity<List<VehicleDto>> getAllVehicles() {
        List<Vehicle> vehicles = vehicleService.findAllVehicles();
        List<VehicleDto> dtos = vehicles.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleDto> getVehicleById(@PathVariable Long id) {
        return vehicleService.findById(id)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Retrieves the registration information for a specific vehicle
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
        try {
            VehicleRegistrationDto registrationDto = vehicleService.getRegistrationInfo(id);
            return ResponseEntity.ok(registrationDto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/registration")
    public ResponseEntity<List<VehicleRegistrationDto>> getAllVehicleRegistrations() {
        List<VehicleRegistrationDto> registrations = vehicleService.getAllRegistrationInfo();
        return ResponseEntity.ok(registrations);
    }

    @PostMapping("/diesel")
    public ResponseEntity<VehicleDto> createDieselVehicle(@RequestBody VehicleDto vehicleDto) {
        try {
            DieselVehicle vehicle = new DieselVehicle();
            vehicle.setVin(vehicleDto.getVin());
            vehicle.setLicensePlate(vehicleDto.getLicensePlate());
            vehicle.setInjectionPumpType(InjectionPumpType.valueOf(vehicleDto.getInjectionPumpType()));
            
            DieselVehicle saved = vehicleService.saveDieselVehicle(vehicle);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/electric")
    public ResponseEntity<VehicleDto> createElectricVehicle(@RequestBody VehicleDto vehicleDto) {
        try {
            ElectricVehicle vehicle = new ElectricVehicle();
            vehicle.setVin(vehicleDto.getVin());
            vehicle.setLicensePlate(vehicleDto.getLicensePlate());
            vehicle.setBatteryType(BatteryType.valueOf(vehicleDto.getBatteryType()));
            vehicle.setBatteryVoltage(vehicleDto.getBatteryVoltage());
            vehicle.setBatteryCurrent(vehicleDto.getBatteryCurrent());
            
            ElectricVehicle saved = vehicleService.saveElectricVehicle(vehicle);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/gas")
    public ResponseEntity<VehicleDto> createGasVehicle(@RequestBody VehicleDto vehicleDto) {
        try {
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
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleDto> updateVehicle(@PathVariable Long id, @RequestBody VehicleDto vehicleDto) {
        try {
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
            } else if (existingVehicle instanceof GasVehicle && vehicleDto.getFuelTypes() != null) {
                Set<FuelType> fuelTypes = Arrays.stream(vehicleDto.getFuelTypes())
                        .map(FuelType::valueOf)
                        .collect(Collectors.toSet());
                ((GasVehicle) existingVehicle).setFuelTypes(fuelTypes);
            }
            
            Vehicle updated = vehicleService.saveVehicle(existingVehicle);
            return ResponseEntity.ok(convertToDto(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        if (vehicleService.deleteVehicle(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<VehicleDto>> getVehiclesByType(@PathVariable String type) {
        try {
            VehicleType vehicleType = VehicleType.valueOf(type.toUpperCase());
            List<Vehicle> vehicles = vehicleService.findByType(vehicleType);
            List<VehicleDto> dtos = vehicles.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/is-convertible")
    public ResponseEntity<Boolean> isVehicleConvertible(@PathVariable Long id) {
        try {
            boolean convertible = vehicleService.isVehicleConvertible(id);
            return ResponseEntity.ok(convertible);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/convert-to-gas")
    public ResponseEntity<VehicleDto> convertElectricToGas(
            @PathVariable Long id, @RequestBody String[] fuelTypeNames) {
        try {
            Set<FuelType> fuelTypes = Arrays.stream(fuelTypeNames)
                    .map(FuelType::valueOf)
                    .collect(Collectors.toSet());
            
            GasVehicle converted = vehicleService.convertElectricToGas(id, fuelTypes);
            return ResponseEntity.ok(convertToDto(converted));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamVehicleEvents() {
        return sseService.createEmitter();
    }

    private VehicleDto convertToDto(Vehicle vehicle) {
        VehicleDto dto = new VehicleDto();
        dto.setId(vehicle.getId());
        dto.setVin(vehicle.getVin());
        dto.setLicensePlate(vehicle.getLicensePlate());
        dto.setType(vehicle.getType());
        dto.setVersion(vehicle.getVersion());

        if (vehicle instanceof DieselVehicle) {
            DieselVehicle dieselVehicle = (DieselVehicle) vehicle;
            dto.setInjectionPumpType(dieselVehicle.getInjectionPumpType().name());
        } else if (vehicle instanceof ElectricVehicle) {
            ElectricVehicle electricVehicle = (ElectricVehicle) vehicle;
            dto.setBatteryType(electricVehicle.getBatteryType().name());
            dto.setBatteryVoltage(electricVehicle.getBatteryVoltage());
            dto.setBatteryCurrent(electricVehicle.getBatteryCurrent());
        } else if (vehicle instanceof GasVehicle) {
            GasVehicle gasVehicle = (GasVehicle) vehicle;
            dto.setFuelTypes(gasVehicle.getFuelTypes().stream()
                    .map(Enum::name)
                    .toArray(String[]::new));
        }
        
        return dto;
    }
}
