package com.l1maor.vehicleworkshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversion_history")
@Audited
public class ConversionHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(nullable = false)
    private LocalDateTime conversionDate;

    @Enumerated(EnumType.STRING)
    private BatteryType originalBatteryType;
    
    private Double originalVoltage;
    
    private Double originalCurrent;

    public ConversionHistory() {
        this.conversionDate = LocalDateTime.now();
    }

    public ConversionHistory(Vehicle vehicle, BatteryType originalBatteryType,
                            Double originalVoltage, Double originalCurrent) {
        this.vehicle = vehicle;
        this.originalBatteryType = originalBatteryType;
        this.originalVoltage = originalVoltage;
        this.originalCurrent = originalCurrent;
        this.conversionDate = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public LocalDateTime getConversionDate() {
        return conversionDate;
    }

    public void setConversionDate(LocalDateTime conversionDate) {
        this.conversionDate = conversionDate;
    }

    public BatteryType getOriginalBatteryType() {
        return originalBatteryType;
    }

    public void setOriginalBatteryType(BatteryType originalBatteryType) {
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
}
