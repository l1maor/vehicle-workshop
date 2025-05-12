package com.l1maor.vehicleworkshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.util.Set;

import org.hibernate.envers.Audited;

@Entity
@Table(name = "vehicles",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"vin"}),
                @UniqueConstraint(columnNames = {"license_plate"})
        })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@Audited
public abstract class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vin", nullable = false, unique = true, length = 17)
    private String vin;

    @Column(name = "license_plate", nullable = false, unique = true, length = 20)
    private String licensePlate;

    @Enumerated(EnumType.STRING)
    @Column(insertable = false, updatable = false)
    private VehicleType type;

    @Version
    private Long version;  // optimistic locking

    @ElementCollection
    @CollectionTable(name = "vehicle_fuel_types", joinColumns = @JoinColumn(name = "vehicle_id"))
    @Column(name = "fuel_type")
    private Set<FuelType> fuelTypes;

    public Vehicle() {
    }

    public Vehicle(String vin, String licensePlate) {
        this.vin = vin;
        this.licensePlate = licensePlate;
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

    public Set<FuelType> getFuelTypes() {
        return fuelTypes;
    }

    public void setFuelTypes(Set<FuelType> fuelTypes) {
        this.fuelTypes = fuelTypes;
    }
}
