package com.l1maor.vehicleworkshop.service;

import com.l1maor.vehicleworkshop.dto.VehicleRegistrationDto;
import com.l1maor.vehicleworkshop.entity.ConversionHistory;
import com.l1maor.vehicleworkshop.entity.DieselVehicle;
import com.l1maor.vehicleworkshop.entity.ElectricVehicle;
import com.l1maor.vehicleworkshop.entity.FuelType;
import com.l1maor.vehicleworkshop.entity.GasVehicle;
import com.l1maor.vehicleworkshop.entity.Vehicle;
import com.l1maor.vehicleworkshop.entity.VehicleType;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VehicleService {
    Vehicle saveVehicle(Vehicle vehicle);
    Optional<Vehicle> findById(Long id);
    Optional<Vehicle> findByVin(String vin);
    Optional<Vehicle> findByLicensePlate(String licensePlate);

    List<Vehicle> findAllVehicles();
    List<Vehicle> findByType(VehicleType type);

    Page<Vehicle> findAllVehiclesPaginated(Pageable pageable);
    Page<Vehicle> findByTypePaginated(VehicleType type, Pageable pageable);
    
    /**
     * Searches for vehicles by VIN or license plate containing the search term, with pagination
     * @param searchTerm The search term to match against VIN or license plate
     * @param pageable Pagination information
     * @return Page of vehicles matching the search criteria
     */
    Page<Vehicle> searchVehicles(String searchTerm, Pageable pageable);
    
    /**
     * Searches for vehicles by VIN or license plate containing the search term,
     * filtered by vehicle type, with pagination
     * @param searchTerm The search term to match against VIN or license plate
     * @param type The vehicle type to filter by
     * @param pageable Pagination information
     * @return Page of vehicles matching the search criteria and type
     */
    Page<Vehicle> searchVehiclesByType(String searchTerm, VehicleType type, Pageable pageable);
    
    boolean deleteVehicle(Long id);

    DieselVehicle saveDieselVehicle(DieselVehicle vehicle);
    ElectricVehicle saveElectricVehicle(ElectricVehicle vehicle);
    GasVehicle saveGasVehicle(GasVehicle vehicle);

    GasVehicle convertElectricToGas(Long vehicleId, Set<FuelType> newFuelTypes);
    
    /**
     * Retrieves the conversion history for a specific vehicle
     * @param vehicleId The ID of the vehicle to retrieve conversion history for
     * @return List of conversion history entries ordered by conversion date (descending)
     */
    List<ConversionHistory> getConversionHistoryForVehicle(Long vehicleId);
    boolean isVehicleConvertible(Long vehicleId);
    boolean isVehicleConvertible(Vehicle vehicle);

    VehicleRegistrationDto getRegistrationInfo(Long vehicleId);

    List<VehicleRegistrationDto> getAllRegistrationInfo();

    Page<VehicleRegistrationDto> getAllRegistrationInfoPaginated(Pageable pageable);
    
    /**
     * Searches for vehicle registration information by VIN or license plate containing the search term, with pagination
     * @param searchTerm The search term to match against VIN or license plate
     * @param pageable Pagination information
     * @return Page of vehicle registration DTOs matching the search criteria
     */
    Page<VehicleRegistrationDto> searchRegistrationInfo(String searchTerm, Pageable pageable);
    
    /**
     * Searches for vehicle registration information by VIN or license plate containing the search term,
     * filtered by vehicle type, with pagination
     * @param searchTerm The search term to match against VIN or license plate
     * @param type The vehicle type to filter by
     * @param pageable Pagination information
     * @return Page of vehicle registration DTOs matching the search criteria and type
     */
    Page<VehicleRegistrationDto> searchRegistrationInfoByType(String searchTerm, VehicleType type, Pageable pageable);

    boolean existsByVin(String vin);
    boolean existsByLicensePlate(String licensePlate);
}
