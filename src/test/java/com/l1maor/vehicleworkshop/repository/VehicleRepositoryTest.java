package com.l1maor.vehicleworkshop.repository;

import com.l1maor.vehicleworkshop.data.TestDataGenerator;
import com.l1maor.vehicleworkshop.entity.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestDataGenerator.class)
public class VehicleRepositoryTest {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private TestDataGenerator dataGenerator;

    @BeforeEach
    void setUp() {
        // Clear database before each test
        vehicleRepository.deleteAll();
    }

    @Test
    void testSaveDieselVehicle() {
        // Given
        DieselVehicle vehicle = dataGenerator.createDieselVehicle();
        
        // When
        DieselVehicle savedVehicle = (DieselVehicle) vehicleRepository.save(vehicle);
        
        // Then
        assertNotNull(savedVehicle.getId());
        assertEquals(vehicle.getVin(), savedVehicle.getVin());
        assertEquals(vehicle.getLicensePlate(), savedVehicle.getLicensePlate());
        assertEquals(vehicle.getInjectionPumpType(), savedVehicle.getInjectionPumpType());
        assertEquals(VehicleType.DIESEL, savedVehicle.getType());
    }

    @Test
    void testSaveElectricVehicle() {
        // Given
        ElectricVehicle vehicle = dataGenerator.createElectricVehicle();
        
        // When
        ElectricVehicle savedVehicle = (ElectricVehicle) vehicleRepository.save(vehicle);
        
        // Then
        assertNotNull(savedVehicle.getId());
        assertEquals(vehicle.getVin(), savedVehicle.getVin());
        assertEquals(vehicle.getLicensePlate(), savedVehicle.getLicensePlate());
        assertEquals(vehicle.getBatteryType(), savedVehicle.getBatteryType());
        assertEquals(vehicle.getBatteryVoltage(), savedVehicle.getBatteryVoltage());
        assertEquals(vehicle.getBatteryCurrent(), savedVehicle.getBatteryCurrent());
        assertEquals(VehicleType.ELECTRIC, savedVehicle.getType());
    }

    @Test
    void testSaveGasVehicle() {
        // Given
        GasVehicle vehicle = dataGenerator.createGasVehicle();
        
        // When
        GasVehicle savedVehicle = (GasVehicle) vehicleRepository.save(vehicle);
        
        // Then
        assertNotNull(savedVehicle.getId());
        assertEquals(vehicle.getVin(), savedVehicle.getVin());
        assertEquals(vehicle.getLicensePlate(), savedVehicle.getLicensePlate());
        assertEquals(vehicle.getFuelTypes().size(), savedVehicle.getFuelTypes().size());
        assertEquals(VehicleType.GASOLINE, savedVehicle.getType());
    }

    @Test
    void testFindByType() {
        // Given
        vehicleRepository.save(dataGenerator.createDieselVehicle());
        vehicleRepository.save(dataGenerator.createDieselVehicle());
        vehicleRepository.save(dataGenerator.createElectricVehicle());
        vehicleRepository.save(dataGenerator.createGasVehicle());
        
        // When
        List<Vehicle> dieselVehicles = vehicleRepository.findByType(VehicleType.DIESEL);
        List<Vehicle> electricVehicles = vehicleRepository.findByType(VehicleType.ELECTRIC);
        List<Vehicle> gasVehicles = vehicleRepository.findByType(VehicleType.GASOLINE);
        
        // Then
        assertEquals(2, dieselVehicles.size());
        assertEquals(1, electricVehicles.size());
        assertEquals(1, gasVehicles.size());
    }

    @Test
    void testExistsByVin() {
        // Given
        DieselVehicle vehicle = dataGenerator.createDieselVehicle();
        vehicleRepository.save(vehicle);
        
        // When & Then
        assertTrue(vehicleRepository.existsByVin(vehicle.getVin()));
        assertFalse(vehicleRepository.existsByVin("NONEXISTENT"));
    }

    @Test
    void testExistsByLicensePlate() {
        // Given
        DieselVehicle vehicle = dataGenerator.createDieselVehicle();
        vehicleRepository.save(vehicle);
        
        // When & Then
        assertTrue(vehicleRepository.existsByLicensePlate(vehicle.getLicensePlate()));
        assertFalse(vehicleRepository.existsByLicensePlate("NONEXISTENT"));
    }

    @Test
    void testFindByVin() {
        // Given
        DieselVehicle vehicle = dataGenerator.createDieselVehicle();
        vehicleRepository.save(vehicle);
        
        // When
        Vehicle found = vehicleRepository.findByVin(vehicle.getVin()).orElse(null);
        
        // Then
        assertNotNull(found);
        assertEquals(vehicle.getId(), found.getId());
    }

    @Test
    void testFindByLicensePlate() {
        // Given
        DieselVehicle vehicle = dataGenerator.createDieselVehicle();
        vehicleRepository.save(vehicle);
        
        // When
        Vehicle found = vehicleRepository.findByLicensePlate(vehicle.getLicensePlate()).orElse(null);
        
        // Then
        assertNotNull(found);
        assertEquals(vehicle.getId(), found.getId());
    }
}
