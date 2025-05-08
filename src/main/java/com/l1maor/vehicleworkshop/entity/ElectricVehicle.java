package com.l1maor.vehicleworkshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.hibernate.envers.Audited;

@Entity
@DiscriminatorValue("ELECTRIC")
@Audited
public class ElectricVehicle extends Vehicle {

    @Enumerated(EnumType.STRING)
    @Column(name = "battery_type")
    private BatteryType batteryType;

    @Column(name = "battery_voltage")
    private Double batteryVoltage;

    @Column(name = "battery_current")
    private Double batteryCurrent;

    public ElectricVehicle() {
        super();
    }

    public ElectricVehicle(String vin, String licensePlate, BatteryType batteryType,
                           Double batteryVoltage, Double batteryCurrent) {
        super(vin, licensePlate);
        this.batteryType = batteryType;
        this.batteryVoltage = batteryVoltage;
        this.batteryCurrent = batteryCurrent;
    }

    public BatteryType getBatteryType() {
        return batteryType;
    }

    public void setBatteryType(BatteryType batteryType) {
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
}
