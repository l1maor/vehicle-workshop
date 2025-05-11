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
@DiscriminatorValue("DIESEL")
@Audited
public class DieselVehicle extends Vehicle {

    @Enumerated(EnumType.STRING)
    @Column(name = "injection_pump_type")
    private InjectionPumpType injectionPumpType;

    public DieselVehicle() {
        super();
        setType(VehicleType.DIESEL);
    }

    public DieselVehicle(String vin, String licensePlate, InjectionPumpType injectionPumpType) {
        super(vin, licensePlate);
        this.injectionPumpType = injectionPumpType;
        setType(VehicleType.DIESEL);
    }

    public InjectionPumpType getInjectionPumpType() {
        return injectionPumpType;
    }

    public void setInjectionPumpType(InjectionPumpType injectionPumpType) {
        this.injectionPumpType = injectionPumpType;
    }
    
    @PostLoad
    @PostPersist
    @PrePersist
    public void ensureType() {
        setType(VehicleType.DIESEL);
    }
}
