package com.l1maor.vehicleworkshop.service;

import com.l1maor.vehicleworkshop.dto.VehicleRegistrationDto;
import com.l1maor.vehicleworkshop.entity.DieselVehicle;
import com.l1maor.vehicleworkshop.entity.ElectricVehicle;
import com.l1maor.vehicleworkshop.entity.FuelType;
import com.l1maor.vehicleworkshop.entity.GasVehicle;
import com.l1maor.vehicleworkshop.entity.Vehicle;
import com.l1maor.vehicleworkshop.entity.VehicleType;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface VehicleService {
    Vehicle saveVehicle(Vehicle vehicle);
    Optional<Vehicle> findById(Long id);
    Optional<Vehicle> findByVin(String vin);
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    List<Vehicle> findAllVehicles();
    List<Vehicle> findByType(VehicleType type);
    boolean deleteVehicle(Long id);

    DieselVehicle saveDieselVehicle(DieselVehicle vehicle);
    ElectricVehicle saveElectricVehicle(ElectricVehicle vehicle);
    GasVehicle saveGasVehicle(GasVehicle vehicle);

    GasVehicle convertElectricToGas(Long vehicleId, Set<FuelType> newFuelTypes);
    boolean isVehicleConvertible(Long vehicleId);
    boolean isVehicleConvertible(Vehicle vehicle);

    VehicleRegistrationDto getRegistrationInfo(Long vehicleId);
    List<VehicleRegistrationDto> getAllRegistrationInfo();

    boolean existsByVin(String vin);
    boolean existsByLicensePlate(String licensePlate);
}
