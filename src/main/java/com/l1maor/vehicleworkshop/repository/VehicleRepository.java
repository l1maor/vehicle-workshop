package com.l1maor.vehicleworkshop.repository;

import com.l1maor.vehicleworkshop.entity.Vehicle;
import com.l1maor.vehicleworkshop.entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long>, RevisionRepository<Vehicle, Long, Long> {
    Optional<Vehicle> findByVin(String vin);
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    List<Vehicle> findByType(VehicleType type);
    boolean existsByVin(String vin);
    boolean existsByLicensePlate(String licensePlate);
}
