package com.l1maor.vehicleworkshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Transient;
import org.hibernate.envers.Audited;

import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("GASOLINE")
@Audited
public class GasVehicle extends Vehicle {

    // Constants for bit flags
    public static final int FUEL_B83 = 1;   // 0001
    public static final int FUEL_B90 = 2;   // 0010
    public static final int FUEL_B94 = 4;   // 0100
    public static final int FUEL_B100 = 8;  // 1000

    @Column(name = "fuel_types_flags")
    private Integer fuelTypesFlags = 0;

    @Transient
    private Set<FuelType> fuelTypes = new HashSet<>();

    public GasVehicle() {
        super();
        setType(VehicleType.GASOLINE);
    }
    
    public GasVehicle(String vin, String licensePlate, Set<FuelType> fuelTypes) {
        super(vin, licensePlate);
        setFuelTypes(fuelTypes);
        setType(VehicleType.GASOLINE);
    }
    
    public Set<FuelType> getFuelTypes() {
        if (fuelTypes.isEmpty() && fuelTypesFlags != null) {
            loadFuelTypesAndEnsureType();
        }
        return fuelTypes;
    }
    
    public void setFuelTypes(Set<FuelType> fuelTypes) {
        this.fuelTypes = fuelTypes;
        saveFuelTypes();
    }
    
    public void addFuelType(FuelType fuelType) {
        this.fuelTypes.add(fuelType);
        this.fuelTypesFlags |= getFlagForFuelType(fuelType);
    }

    public void removeFuelType(FuelType fuelType) {
        this.fuelTypes.remove(fuelType);
        this.fuelTypesFlags &= ~getFlagForFuelType(fuelType);
    }
    
    public boolean usesFuel(FuelType fuelType) {
        return (fuelTypesFlags & getFlagForFuelType(fuelType)) != 0;
    }
    
    @PostLoad
    public void loadFuelTypesAndEnsureType() {
        fuelTypes.clear();
        if (fuelTypesFlags == null) return;
        
        if ((fuelTypesFlags & FUEL_B83) != 0) fuelTypes.add(FuelType.B83);
        if ((fuelTypesFlags & FUEL_B90) != 0) fuelTypes.add(FuelType.B90);
        if ((fuelTypesFlags & FUEL_B94) != 0) fuelTypes.add(FuelType.B94);
        if ((fuelTypesFlags & FUEL_B100) != 0) fuelTypes.add(FuelType.B100);
        
        // Ensure vehicle type
        setType(VehicleType.GASOLINE);
    }
    
    @PrePersist
    @PreUpdate
    public void saveFuelTypes() {
        fuelTypesFlags = 0;
        for (FuelType type : fuelTypes) {
            fuelTypesFlags |= getFlagForFuelType(type);
        }
    }
    
    private int getFlagForFuelType(FuelType fuelType) {
        switch(fuelType) {
            case B83: return FUEL_B83;
            case B90: return FUEL_B90;
            case B94: return FUEL_B94;
            case B100: return FUEL_B100;
            default: throw new IllegalArgumentException("Unknown fuel type: " + fuelType);
        }
    }
    
    @PostPersist
    public void ensureTypeAfterPersist() {
        setType(VehicleType.GASOLINE);
    }
}
