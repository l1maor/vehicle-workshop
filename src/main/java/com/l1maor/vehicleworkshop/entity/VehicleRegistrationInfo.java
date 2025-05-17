package com.l1maor.vehicleworkshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Synchronize;

/**
 * Entity representing the vw_vehicle_registration_info database view.
 * 
 * This is a read-only entity mapped to a database view that provides formatted 
 * registration information for different vehicle types. The underlying view has
 * a 'vw_' prefix to clearly distinguish it from tables in the database schema.
 * 
 * The view performs formatting for different vehicle types:
 * - Diesel: License plate + injection pump type
 * - Electric: VIN + battery voltage + battery current + battery type
 * - Gasoline: License plate + supported fuel types
 *
 * This entity uses @Subselect to tell Hibernate it's a view created by SQL,
 * not a table that Hibernate should try to manage or create.
 * 
 * The @Synchronize annotation ensures Hibernate knows the entity depends on the vehicles table
 * so it can properly handle caching and updates when the underlying table changes.
 */
@Entity
@Immutable
@Table(name = "vw_vehicle_registration_info")
@Subselect("SELECT * FROM vw_vehicle_registration_info")
@Synchronize({"vehicles"})
public class VehicleRegistrationInfo {

    @Id
    private Long id;
    
    @Column(name = "type")
    private String type;
    
    @Column(name = "registration_info")
    private String registrationInfo;
    
    @Column(name = "is_convertible")
    private Boolean convertible;
    
    @Column(name = "conversion_data")
    private String conversionData;
    
    // Getters and setters
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRegistrationInfo() {
        return registrationInfo;
    }

    public void setRegistrationInfo(String registrationInfo) {
        this.registrationInfo = registrationInfo;
    }

    public Boolean getConvertible() {
        return convertible;
    }

    public void setConvertible(Boolean convertible) {
        this.convertible = convertible;
    }

    public String getConversionData() {
        return conversionData;
    }

    public void setConversionData(String conversionData) {
        this.conversionData = conversionData;
    }
}
