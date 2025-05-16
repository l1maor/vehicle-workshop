package com.l1maor.vehicleworkshop.repository;

import com.l1maor.vehicleworkshop.data.TestDataGenerator;
import com.l1maor.vehicleworkshop.entity.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;


import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestDataGenerator.class, com.l1maor.vehicleworkshop.config.TestConfig.class})
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

        DieselVehicle vehicle = dataGenerator.createDieselVehicle(1);


        DieselVehicle savedVehicle = (DieselVehicle) vehicleRepository.save(vehicle);


        assertNotNull(savedVehicle.getId());
        assertEquals(vehicle.getVin(), savedVehicle.getVin());
        assertEquals(vehicle.getLicensePlate(), savedVehicle.getLicensePlate());
        assertEquals(vehicle.getInjectionPumpType(), savedVehicle.getInjectionPumpType());
        assertEquals(VehicleType.DIESEL, savedVehicle.getType());
    }

    @Test
    void testSaveElectricVehicle() {

        ElectricVehicle vehicle = dataGenerator.createElectricVehicle(1);


        ElectricVehicle savedVehicle = (ElectricVehicle) vehicleRepository.save(vehicle);


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

        GasVehicle vehicle = dataGenerator.createGasVehicle(1);


        GasVehicle savedVehicle = (GasVehicle) vehicleRepository.save(vehicle);


        assertNotNull(savedVehicle.getId());
        assertEquals(vehicle.getVin(), savedVehicle.getVin());
        assertEquals(vehicle.getLicensePlate(), savedVehicle.getLicensePlate());
        assertEquals(vehicle.getFuelTypes().size(), savedVehicle.getFuelTypes().size());
        assertEquals(VehicleType.GASOLINE, savedVehicle.getType());
    }

    @Test
    void testFindByType() {
        vehicleRepository.save(dataGenerator.createDieselVehicle(1));
        vehicleRepository.save(dataGenerator.createDieselVehicle(2));
        vehicleRepository.save(dataGenerator.createElectricVehicle(1));
        vehicleRepository.save(dataGenerator.createGasVehicle(1));


        List<Vehicle> dieselVehicles = vehicleRepository.findByType(VehicleType.DIESEL);
        List<Vehicle> electricVehicles = vehicleRepository.findByType(VehicleType.ELECTRIC);
        List<Vehicle> gasVehicles = vehicleRepository.findByType(VehicleType.GASOLINE);


        assertEquals(2, dieselVehicles.size());
        assertEquals(1, electricVehicles.size());
        assertEquals(1, gasVehicles.size());
    }

    @Test
    void testExistsByVin() {

        DieselVehicle vehicle = dataGenerator.createDieselVehicle(1);
        vehicleRepository.save(vehicle);


        assertTrue(vehicleRepository.existsByVin(vehicle.getVin()));
        assertFalse(vehicleRepository.existsByVin("NONEXISTENT"));
    }

    @Test
    void testExistsByLicensePlate() {

        DieselVehicle vehicle = dataGenerator.createDieselVehicle(2);
        vehicleRepository.save(vehicle);


        assertTrue(vehicleRepository.existsByLicensePlate(vehicle.getLicensePlate()));
        assertFalse(vehicleRepository.existsByLicensePlate("NONEXISTENT"));
    }

    @Test
    void testFindByVin() {

        DieselVehicle vehicle = dataGenerator.createDieselVehicle(3);
        vehicleRepository.save(vehicle);


        Vehicle found = vehicleRepository.findByVin(vehicle.getVin()).orElse(null);


        assertNotNull(found);
        assertEquals(vehicle.getId(), found.getId());
    }

    @Test
    void testFindByLicensePlate() {

        DieselVehicle vehicle = dataGenerator.createDieselVehicle(4);
        vehicleRepository.save(vehicle);


        Vehicle found = vehicleRepository.findByLicensePlate(vehicle.getLicensePlate()).orElse(null);


        assertNotNull(found);
        assertEquals(vehicle.getId(), found.getId());
    }
}
