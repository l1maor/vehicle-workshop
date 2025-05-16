package com.l1maor.vehicleworkshop.repository;

import com.l1maor.vehicleworkshop.entity.ConversionHistory;
import com.l1maor.vehicleworkshop.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversionHistoryRepository extends JpaRepository<ConversionHistory, Long> {
    List<ConversionHistory> findByVehicleOrderByConversionDateDesc(Vehicle vehicle);
    List<ConversionHistory> findByVehicleId(Long vehicleId);
    

    Page<ConversionHistory> findByVehicleOrderByConversionDateDesc(Vehicle vehicle, Pageable pageable);
    Page<ConversionHistory> findByVehicleId(Long vehicleId, Pageable pageable);

    long countByVehicleId(Long vehicleId);
    
    List<ConversionHistory> findByVehicleIdOrderByConversionDateDesc(Long vehicleId);
}
