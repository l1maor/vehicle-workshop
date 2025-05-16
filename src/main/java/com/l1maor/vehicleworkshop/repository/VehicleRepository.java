package com.l1maor.vehicleworkshop.repository;

import com.l1maor.vehicleworkshop.entity.Vehicle;
import com.l1maor.vehicleworkshop.entity.VehicleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long>, RevisionRepository<Vehicle, Long, Long> {
    Optional<Vehicle> findByVin(String vin);
    
    Optional<Vehicle> findByLicensePlate(String licensePlate);

    List<Vehicle> findByType(VehicleType type);

    Page<Vehicle> findByType(VehicleType type, Pageable pageable);
    
    boolean existsByVin(String vin);
    boolean existsByLicensePlate(String licensePlate);
    
    /**
     * Search for vehicles by VIN or license plate using case-insensitive matching
     * @param searchTerm The search term to match against VIN or license plate
     * @param pageable Pagination and sorting parameters
     * @return A page of vehicles matching the search criteria
     */
    @Query("SELECT v FROM Vehicle v WHERE LOWER(v.vin) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(v.licensePlate) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Vehicle> findByVinOrLicensePlateContaining(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    
    /**
     * Search by VIN or license plate with type filter
     * @param searchTerm The search term to match against VIN or license plate
     * @param type The vehicle type
     * @param pageable Pagination and sorting parameters
     * @return A page of vehicles matching the search criteria
     */
    @Query("SELECT v FROM Vehicle v WHERE v.type = :type AND (LOWER(v.vin) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(v.licensePlate) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Vehicle> findByTypeAndSearch(@Param("type") VehicleType type, @Param("searchTerm") String searchTerm, Pageable pageable);
            
    /**
     * Utility method to bridge between string-based type filter and actual repository methods
     * @param searchTerm The search term to match against VIN or license plate
     * @param typeStr The vehicle type as a string
     * @param pageable Pagination and sorting parameters
     * @return A page of vehicles matching the search criteria
     */
    /**
     * Utility method to bridge between string-based type filter and actual repository methods
     * Used by the controller layer to handle type filtering with proper error handling
     * @param searchTerm The search term to match against VIN or license plate
     * @param typeStr The vehicle type as a string
     * @param pageable Pagination and sorting parameters
     * @return A page of vehicles matching the search criteria
     */
    default Page<Vehicle> findByVinOrLicensePlateContainingAndType(
            String searchTerm, String typeStr, Pageable pageable) {
        Logger logger = LoggerFactory.getLogger(VehicleRepository.class);
        
        logger.debug("Searching vehicles by term: '{}' and type: '{}'", searchTerm, typeStr);
        
        if (typeStr == null || typeStr.trim().isEmpty()) {
            return findByVinOrLicensePlateContaining(searchTerm, pageable);
        }
        
        try {
            VehicleType type = VehicleType.valueOf(typeStr);
            return findByTypeAndSearch(type, searchTerm, pageable);
        } catch (IllegalArgumentException e) {
            // If the type is invalid, log the error and return an empty page
            logger.warn("Invalid vehicle type: {}", typeStr);
            return Page.empty(pageable);
        } catch (Exception e) {
            // For any other error, log and return an empty page
            logger.error("Error in search: {}", e.getMessage());
            return Page.empty(pageable);
        }
    }
}
