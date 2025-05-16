package com.l1maor.vehicleworkshop.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.l1maor.vehicleworkshop.entity.VehicleRegistrationInfo;

@Repository
public interface VehicleRegistrationInfoRepository extends JpaRepository<VehicleRegistrationInfo, Long> {
    
    /**
     * Native query that uses the view directly
     */
    @Query(value = "SELECT * FROM vw_vehicle_registration_info", nativeQuery = true)
    List<VehicleRegistrationInfo> findAllVehicleRegistrationsNative();
    
    /**
     * Native query with pagination support that uses the view directly
     */
    @Query(value = "SELECT * FROM vw_vehicle_registration_info", 
            countQuery = "SELECT COUNT(*) FROM vw_vehicle_registration_info",
            nativeQuery = true)
    Page<VehicleRegistrationInfo> findAllVehicleRegistrationsNativePaginated(Pageable pageable);
}
