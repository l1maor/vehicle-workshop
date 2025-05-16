package com.l1maor.vehicleworkshop.service;

import com.l1maor.vehicleworkshop.dto.VehicleDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class ValidationService {

    private static final Logger logger = LoggerFactory.getLogger(ValidationService.class);
    private static final Pattern VIN_PATTERN = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$");
    
    /**
     * Validates a VIN (Vehicle Identification Number)
     * 
     * @param vin The VIN to validate
     * @return true if the VIN is valid, false otherwise
     */
    public boolean isValidVin(String vin) {
        logger.debug("Validating VIN: {}", vin != null ? vin : "null");
        
        if (vin == null) {
            logger.debug("VIN validation failed: null value");
            return false;
        }
        
        if (vin.length() != 17) {
            logger.debug("VIN validation failed: incorrect length {}", vin.length());
            return false;
        }
        
        boolean isValid = VIN_PATTERN.matcher(vin).matches();
        if (isValid) {
            logger.debug("VIN validation succeeded for: {}", vin);
        } else {
            logger.debug("VIN validation failed: contains invalid characters");
        }
        
        return isValid;
    }
    
    /**
     * Validates the vehicle-type-specific fields for a vehicle DTO
     * 
     * @param vehicleDto The vehicle DTO to validate
     * @return A map of field validation errors, empty if all fields are valid
     */
    public Map<String, String> validateVehicleTypeSpecificFields(VehicleDto vehicleDto) {
        logger.debug("Validating vehicle type-specific fields for vehicle: {}", 
                vehicleDto.getId() != null ? vehicleDto.getId() : "new vehicle");
        
        Map<String, String> errors = new HashMap<>();
        
        if (vehicleDto.getType() == null) {
            logger.debug("Validation failed: vehicle type is null");
            errors.put("type", "Vehicle type is required");
            return errors;
        }
        
        logger.debug("Validating fields for vehicle type: {}", vehicleDto.getType());
        
        switch (vehicleDto.getType()) {
            case DIESEL:
                logger.debug("Validating diesel vehicle fields");
                if (vehicleDto.getInjectionPumpType() == null || vehicleDto.getInjectionPumpType().isEmpty()) {
                    logger.debug("Validation failed: injection pump type is missing");
                    errors.put("injectionPumpType", "Injection pump type is required for diesel vehicles");
                } else {
                    logger.debug("Injection pump type is valid: {}", vehicleDto.getInjectionPumpType());
                }
                break;
                
            case ELECTRIC:
                logger.debug("Validating electric vehicle fields");
                if (vehicleDto.getBatteryType() == null || vehicleDto.getBatteryType().isEmpty()) {
                    logger.debug("Validation failed: battery type is missing");
                    errors.put("batteryType", "Battery type is required for electric vehicles");
                } else {
                    logger.debug("Battery type is valid: {}", vehicleDto.getBatteryType());
                }
                
                if (vehicleDto.getBatteryVoltage() == null) {
                    logger.debug("Validation failed: battery voltage is missing");
                    errors.put("batteryVoltage", "Battery voltage is required for electric vehicles");
                } else if (vehicleDto.getBatteryVoltage() < 0 || vehicleDto.getBatteryVoltage() > 1000) {
                    logger.debug("Validation failed: battery voltage out of range: {}", vehicleDto.getBatteryVoltage());
                    errors.put("batteryVoltage", "Battery voltage must be between 0 and 1000");
                } else {
                    logger.debug("Battery voltage is valid: {}", vehicleDto.getBatteryVoltage());
                }
                
                if (vehicleDto.getBatteryCurrent() == null) {
                    logger.debug("Validation failed: battery current is missing");
                    errors.put("batteryCurrent", "Battery current is required for electric vehicles");
                } else if (vehicleDto.getBatteryCurrent() < 0 || vehicleDto.getBatteryCurrent() > 1000) {
                    logger.debug("Validation failed: battery current out of range: {}", vehicleDto.getBatteryCurrent());
                    errors.put("batteryCurrent", "Battery current must be between 0 and 1000");
                } else {
                    logger.debug("Battery current is valid: {}", vehicleDto.getBatteryCurrent());
                }
                break;
                
            case GASOLINE:
                logger.debug("Validating gasoline vehicle fields");
                if (vehicleDto.getFuelTypes() == null || vehicleDto.getFuelTypes().length == 0) {
                    logger.debug("Validation failed: no fuel types specified");
                    errors.put("fuelTypes", "At least one fuel type is required for gasoline vehicles");
                } else {
                    logger.debug("Fuel types are valid: count={}", vehicleDto.getFuelTypes().length);
                }
                break;
                
            default:
                logger.warn("Unknown vehicle type encountered during validation: {}", vehicleDto.getType());
                errors.put("type", "Unknown vehicle type: " + vehicleDto.getType());
        }
        
        if (errors.isEmpty()) {
            logger.debug("Vehicle type-specific validation succeeded for type: {}", vehicleDto.getType());
        } else {
            logger.debug("Vehicle type-specific validation failed with {} errors", errors.size());
        }
        
        return errors;
    }
}
