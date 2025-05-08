package com.l1maor.vehicleworkshop.dto;

import com.l1maor.vehicleworkshop.entity.VehicleType;

/**
 * DTO for returning formatted registration information based on vehicle type:
 * - Diesel vehicles: License plate + type of injection pump
 * - Electric vehicles: VIN + Voltage + Current + Battery Type
 * - Gasoline vehicles: License plate + Type of fuel used
 * If a vehicle is convertible (electric), it also includes conversion data.
 */
public class VehicleRegistrationDto {
    private Long id;
    private VehicleType type;
    private String registrationInfo;
    private boolean convertible;
    private String conversionData;
    
    public VehicleRegistrationDto() {
    }

    public VehicleRegistrationDto(Long id, VehicleType type, String registrationInfo, boolean convertible, String conversionData) {
        this.id = id;
        this.type = type;
        this.registrationInfo = registrationInfo;
        this.convertible = convertible;
        this.conversionData = conversionData;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    public String getRegistrationInfo() {
        return registrationInfo;
    }

    public void setRegistrationInfo(String registrationInfo) {
        this.registrationInfo = registrationInfo;
    }

    public boolean isConvertible() {
        return convertible;
    }

    public void setConvertible(boolean convertible) {
        this.convertible = convertible;
    }

    public String getConversionData() {
        return conversionData;
    }

    public void setConversionData(String conversionData) {
        this.conversionData = conversionData;
    }
}
