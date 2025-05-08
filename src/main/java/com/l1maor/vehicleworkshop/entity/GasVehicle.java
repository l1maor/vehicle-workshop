package com.l1maor.vehicleworkshop.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import org.hibernate.envers.Audited;

import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("GASOLINE")
@Audited
public class GasVehicle extends Vehicle {

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "vehicle_fuels", joinColumns = @JoinColumn(name = "vehicle_id"))
    @Column(name = "fuel_type")
    @Enumerated(EnumType.STRING)
    private Set<FuelType> fuelTypes = new HashSet<>();

    public GasVehicle() {
        super();
    }

    public GasVehicle(String vin, String licensePlate, Set<FuelType> fuelTypes) {
        super(vin, licensePlate);
        this.fuelTypes = fuelTypes;
    }

    public Set<FuelType> getFuelTypes() {
        return fuelTypes;
    }

    public void setFuelTypes(Set<FuelType> fuelTypes) {
        this.fuelTypes = fuelTypes;
    }

    public void addFuelType(FuelType fuelType) {
        this.fuelTypes.add(fuelType);
    }

    public void removeFuelType(FuelType fuelType) {
        this.fuelTypes.remove(fuelType);
    }
}
