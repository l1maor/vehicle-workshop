package com.l1maor.vehicleworkshop.dto;

import java.time.LocalDateTime;

public class ConversionHistoryDto {
    private Long id;
    private Long vehicleId;
    private String fromType;
    private String toType;
    private LocalDateTime conversionDate;
    private String originalBatteryType;
    private Double originalVoltage;
    private Double originalCurrent;
    private String conversionDetails;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getFromType() {
        return fromType;
    }

    public void setFromType(String fromType) {
        this.fromType = fromType;
    }

    public String getToType() {
        return toType;
    }

    public void setToType(String toType) {
        this.toType = toType;
    }

    public LocalDateTime getConversionDate() {
        return conversionDate;
    }

    public void setConversionDate(LocalDateTime conversionDate) {
        this.conversionDate = conversionDate;
    }
    
    public String getOriginalBatteryType() {
        return originalBatteryType;
    }
    
    public void setOriginalBatteryType(String originalBatteryType) {
        this.originalBatteryType = originalBatteryType;
    }
    
    public Double getOriginalVoltage() {
        return originalVoltage;
    }
    
    public void setOriginalVoltage(Double originalVoltage) {
        this.originalVoltage = originalVoltage;
    }
    
    public Double getOriginalCurrent() {
        return originalCurrent;
    }
    
    public void setOriginalCurrent(Double originalCurrent) {
        this.originalCurrent = originalCurrent;
    }
    
    public String getConversionDetails() {
        return conversionDetails;
    }
    
    public void setConversionDetails(String conversionDetails) {
        this.conversionDetails = conversionDetails;
    }
}
