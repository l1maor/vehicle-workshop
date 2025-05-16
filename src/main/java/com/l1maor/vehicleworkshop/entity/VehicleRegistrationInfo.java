package com.l1maor.vehicleworkshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/**
 * Entity representing the vehicle_registration_info view
 * This is a read-only entity mapped to a database view
 */
@Entity
@Immutable
@Table(name = "vw_vehicle_registration_info")
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
