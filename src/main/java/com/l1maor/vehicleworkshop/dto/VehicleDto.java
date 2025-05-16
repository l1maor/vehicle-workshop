package com.l1maor.vehicleworkshop.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.l1maor.vehicleworkshop.entity.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public class VehicleDto {
    private Long id;
    
    @NotBlank(message = "VIN is required")
    @Size(min = 17, max = 17, message = "VIN must be exactly 17 characters")
    @Pattern(regexp = "^[A-HJ-NPR-Z0-9]{17}$", message = "VIN format is invalid")
    private String vin;
    
    @NotBlank(message = "License plate is required")
    @Size(max = 20, message = "License plate cannot exceed 20 characters")
    private String licensePlate;
    
    @NotNull(message = "Vehicle type is required")
    private VehicleType type;
    
    private Long version;
    
    private Boolean convertible;

    // Only required for diesel vehicles
    private String injectionPumpType;

    // Only required for electric vehicles
    private String batteryType;
    
    @Min(value = 0, message = "Battery voltage cannot be negative")
    @Max(value = 1000, message = "Battery voltage cannot exceed 1000V")
    private Integer batteryVoltage;
    
    @Min(value = 0, message = "Battery current cannot be negative")
    @Max(value = 1000, message = "Battery current cannot exceed 1000A")
    private Integer batteryCurrent;

    // Only required for gas vehicles
    private String[] fuelTypes;

    public VehicleDto() {
    }

    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getVin() {
        return vin;
    }
    
    public void setVin(String vin) {
        this.vin = vin;
    }
    
    public String getLicensePlate() {
        return licensePlate;
    }
    
    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }
    
    public VehicleType getType() {
        return type;
    }
    
    public void setType(VehicleType type) {
        this.type = type;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
    
    public String getInjectionPumpType() {
        return injectionPumpType;
    }
    
    public void setInjectionPumpType(String injectionPumpType) {
        this.injectionPumpType = injectionPumpType;
    }
    
    public String getBatteryType() {
        return batteryType;
    }
    
    public void setBatteryType(String batteryType) {
        this.batteryType = batteryType;
    }
    
    @JsonProperty
    public Integer getBatteryVoltage() {
        // Return the actual database value without defaults
        return batteryVoltage;
    }
    
    public void setBatteryVoltage(Integer batteryVoltage) {
        this.batteryVoltage = batteryVoltage;
    }

    @JsonIgnore
    public void setBatteryVoltage(Double batteryVoltage) {
        if (batteryVoltage != null) {
            setBatteryVoltage(batteryVoltage.intValue());
        }
    }
    
    @JsonProperty
    public Integer getBatteryCurrent() {
        // Return the actual database value without defaults
        return batteryCurrent;
    }
    
    public void setBatteryCurrent(Integer batteryCurrent) {
        this.batteryCurrent = batteryCurrent;
    }

    @JsonIgnore
    public void setBatteryCurrent(Double batteryCurrent) {
        if (batteryCurrent != null) {
            setBatteryCurrent(batteryCurrent.intValue());
        }
    }
    
    public String[] getFuelTypes() {
        return fuelTypes;
    }
    
    public void setFuelTypes(String[] fuelTypes) {
        this.fuelTypes = fuelTypes;
    }
    
    public Boolean isConvertible() {
        return convertible;
    }
    
    public void setConvertible(Boolean convertible) {
        this.convertible = convertible;
    }
}
