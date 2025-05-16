package com.l1maor.vehicleworkshop.data;

import com.l1maor.vehicleworkshop.entity.*;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

@Component
public class TestDataGenerator {

    public DieselVehicle createDieselVehicle(int index) {
        String uuid = "D" + System.currentTimeMillis() % 10000000 + index % 1000;
        DieselVehicle vehicle = new DieselVehicle();
        vehicle.setVin(uuid);
        vehicle.setLicensePlate("D-" + System.nanoTime() % 10000000 + index);
        vehicle.setType(VehicleType.DIESEL);
        vehicle.setInjectionPumpType(index % 2 == 0 ? InjectionPumpType.LINEAR : InjectionPumpType.ROTARY);
        vehicle.setConvertible(false);
        return vehicle;
    }

    public ElectricVehicle createElectricVehicle(int index) {
        String uuid = "E" + System.currentTimeMillis() % 10000000 + index % 1000;
        ElectricVehicle vehicle = new ElectricVehicle();
        vehicle.setVin(uuid);
        vehicle.setLicensePlate("E-" + System.nanoTime() % 10000000 + index);
        vehicle.setType(VehicleType.ELECTRIC);
        vehicle.setBatteryType(BatteryType.LITHIUM);
        vehicle.setBatteryVoltage(240);
        vehicle.setBatteryCurrent(30);
        vehicle.setConvertible(true);
        return vehicle;
    }

    public GasVehicle createGasVehicle(int index) {
        String uuid = "G" + System.currentTimeMillis() % 10000000 + index % 1000;
        GasVehicle vehicle = new GasVehicle();
        vehicle.setVin(uuid);
        vehicle.setLicensePlate("G-" + System.nanoTime() % 10000000 + index);
        vehicle.setType(VehicleType.GASOLINE);
        vehicle.setFuelTypes(EnumSet.of(FuelType.B83, FuelType.B94));
        vehicle.setConvertible(false);
        return vehicle;
    }

    // Create vehicles with specific VIN and license plate for search testing
    public DieselVehicle createDieselVehicleWithSpecificData(String vin, String licensePlate) {
        DieselVehicle vehicle = new DieselVehicle();
        vehicle.setVin(vin);
        vehicle.setLicensePlate(licensePlate);
        vehicle.setType(VehicleType.DIESEL);
        vehicle.setInjectionPumpType(InjectionPumpType.LINEAR);
        vehicle.setConvertible(false);
        return vehicle;
    }

    public ElectricVehicle createElectricVehicleWithSpecificData(String vin, String licensePlate) {
        ElectricVehicle vehicle = new ElectricVehicle();
        vehicle.setVin(vin);
        vehicle.setLicensePlate(licensePlate);
        vehicle.setType(VehicleType.ELECTRIC);
        vehicle.setBatteryType(BatteryType.LITHIUM);
        vehicle.setBatteryVoltage(240.0);
        vehicle.setBatteryCurrent(30.0);
        vehicle.setConvertible(true);
        return vehicle;
    }

    public GasVehicle createGasVehicleWithSpecificData(String vin, String licensePlate) {
        GasVehicle vehicle = new GasVehicle();
        vehicle.setVin(vin);
        vehicle.setLicensePlate(licensePlate);
        vehicle.setType(VehicleType.GASOLINE);
        vehicle.setFuelTypes(EnumSet.of(FuelType.B83, FuelType.B94));
        vehicle.setConvertible(false);
        return vehicle;
    }
    
    // Stub methods for compatibility with existing tests
    public void clearAllData() {
        // No-op for unit tests
    }
    
    public User createUser(String username, String password, RoleType roleType) {
        // This is a stub implementation to satisfy compilation
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(password); // In real code, this would be hashed
        user.setRoleType(roleType);
        return user;
    }
}