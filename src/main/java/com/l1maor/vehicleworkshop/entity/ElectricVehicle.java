package com.l1maor.vehicleworkshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PrePersist;
import org.hibernate.envers.Audited;

@Entity
@DiscriminatorValue("ELECTRIC")
@Audited
public class ElectricVehicle extends Vehicle {

    @Enumerated(EnumType.STRING)
    @Column(name = "battery_type")
    private BatteryType batteryType;

    @Column(name = "battery_voltage")
    private Integer batteryVoltage;

    @Column(name = "battery_current")
    private Integer batteryCurrent;

    public ElectricVehicle() {
        super();
        setType(VehicleType.ELECTRIC);
        setConvertible(true); // By default, electric vehicles are convertible
    }

    public ElectricVehicle(String vin, String licensePlate, BatteryType batteryType,
                           Integer batteryVoltage, Integer batteryCurrent) {
        this(vin, licensePlate, batteryType, batteryVoltage, batteryCurrent, true);
    }
    
    public ElectricVehicle(String vin, String licensePlate, BatteryType batteryType,
                           Integer  batteryVoltage, Integer batteryCurrent, Boolean convertible) {
        super(vin, licensePlate);
        this.batteryType = batteryType;
        this.batteryVoltage = batteryVoltage;
        this.batteryCurrent = batteryCurrent;
        setType(VehicleType.ELECTRIC);
        setConvertible(convertible);
    }

    public BatteryType getBatteryType() {
        return batteryType;
    }

    public void setBatteryType(BatteryType batteryType) {
        this.batteryType = batteryType;
    }

    public Integer getBatteryVoltage() {
        return batteryVoltage;
    }

    public void setBatteryVoltage(Integer batteryVoltage) {
        if (batteryVoltage != null && (batteryVoltage < 0 || batteryVoltage > 1000)) {
            throw new IllegalArgumentException("Battery voltage must be between 0 and 1000 volts");
        }
        this.batteryVoltage = batteryVoltage;
    }

    public void setBatteryVoltage(Double batteryVoltage) {
        if (batteryVoltage != null) {
            setBatteryVoltage(batteryVoltage.intValue());
        }
    }

    public Integer getBatteryCurrent() {
        return batteryCurrent;
    }

    public void setBatteryCurrent(Integer batteryCurrent) {
        if (batteryCurrent != null && (batteryCurrent < 0 || batteryCurrent > 1000)) {
            throw new IllegalArgumentException("Battery current must be between 0 and 1000 amperes");
        }
        this.batteryCurrent = batteryCurrent;
    }

    public void setBatteryCurrent(Double batteryCurrent) {
        if (batteryCurrent != null) {
            setBatteryCurrent(batteryCurrent.intValue());
        }
    }
    
    @PostLoad
    @PostPersist
    @PrePersist
    public void ensureType() {
        setType(VehicleType.ELECTRIC);
    }
}
