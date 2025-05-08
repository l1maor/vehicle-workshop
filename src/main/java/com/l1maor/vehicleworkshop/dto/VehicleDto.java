package com.l1maor.vehicleworkshop.dto;

import com.l1maor.vehicleworkshop.entity.VehicleType;

public class VehicleDto {
    private Long id;
    private String vin;
    private String licensePlate;
    private VehicleType type;
    private Long version;

    private String injectionPumpType;

    private String batteryType;
    private Double batteryVoltage;
    private Double batteryCurrent;

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
    
    public Double getBatteryVoltage() {
        return batteryVoltage;
    }
    
    public void setBatteryVoltage(Double batteryVoltage) {
        this.batteryVoltage = batteryVoltage;
    }
    
    public Double getBatteryCurrent() {
        return batteryCurrent;
    }
    
    public void setBatteryCurrent(Double batteryCurrent) {
        this.batteryCurrent = batteryCurrent;
    }
    
    public String[] getFuelTypes() {
        return fuelTypes;
    }
    
    public void setFuelTypes(String[] fuelTypes) {
        this.fuelTypes = fuelTypes;
    }
}
