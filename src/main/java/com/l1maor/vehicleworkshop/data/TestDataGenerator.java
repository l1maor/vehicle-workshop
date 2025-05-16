package com.l1maor.vehicleworkshop.data;

import com.l1maor.vehicleworkshop.entity.*;
import com.l1maor.vehicleworkshop.repository.ConversionHistoryRepository;
import com.l1maor.vehicleworkshop.repository.UserRepository;
import com.l1maor.vehicleworkshop.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class TestDataGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(TestDataGenerator.class);

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ConversionHistoryRepository conversionHistoryRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @PersistenceContext
    private EntityManager entityManager;

    public DieselVehicle createDieselVehicle(int index) {
        String uuid = "D" + System.currentTimeMillis() % 10000000 + index % 1000;
        DieselVehicle vehicle = new DieselVehicle();
        vehicle.setVin(uuid);
        vehicle.setLicensePlate("D-" + System.nanoTime() % 10000000 + index);
        vehicle.setInjectionPumpType(index % 2 == 0 ? InjectionPumpType.LINEAR : InjectionPumpType.ROTARY);
        return vehicle;
    }

    public ElectricVehicle createElectricVehicle(int index) {
        String uuid = "E" + System.currentTimeMillis() % 10000000 + index % 1000;
        ElectricVehicle vehicle = new ElectricVehicle();
        vehicle.setVin(uuid);
        vehicle.setLicensePlate("E-" + System.nanoTime() % 10000000 + index);
        vehicle.setBatteryType(index % 2 == 0 ? BatteryType.GEL : BatteryType.LITHIUM);
        
        // Generate random voltage between 24 and 800 volts (common EV battery voltage ranges)
        Random random = new Random();
        int voltage = 24 + random.nextInt(777); // Results in range 24-800
        vehicle.setBatteryVoltage(voltage);
        
        // Generate random current between 50 and 800 amperes
        int current = 50 + random.nextInt(751); // Results in range 50-800
        vehicle.setBatteryCurrent(current);
        
        vehicle.setConvertible(true);
        return vehicle;
    }

    public GasVehicle createGasVehicle(int index) {
        String uuid = "G" + System.currentTimeMillis() % 10000000 + index % 1000;
        GasVehicle vehicle = new GasVehicle();
        vehicle.setVin(uuid);
        vehicle.setLicensePlate("G-" + System.nanoTime() % 10000000 + index);
        
        // Add random fuel types
        Set<FuelType> fuelTypes = new HashSet<>();
        int fuelTypeFlags = 1 + new Random().nextInt(15);
        
        if ((fuelTypeFlags & GasVehicle.FUEL_B83) != 0) fuelTypes.add(FuelType.B83);
        if ((fuelTypeFlags & GasVehicle.FUEL_B90) != 0) fuelTypes.add(FuelType.B90);
        if ((fuelTypeFlags & GasVehicle.FUEL_B94) != 0) fuelTypes.add(FuelType.B94);
        if ((fuelTypeFlags & GasVehicle.FUEL_B100) != 0) fuelTypes.add(FuelType.B100);
        
        vehicle.setFuelTypes(fuelTypes);
        
        return vehicle;
    }

    @Transactional
    public User createUser(String username, String password, RoleType roleType) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRoleType(roleType);
        
        return userRepository.save(user);
    }
    
    @Transactional
    public ConversionHistory createConversionHistory(ElectricVehicle vehicle) {
        ConversionHistory history = new ConversionHistory();
        history.setVehicle(vehicle);
        history.setConversionDate(LocalDateTime.now().minusDays(new Random().nextInt(30)));
        history.setOriginalBatteryType(vehicle.getBatteryType());
        history.setOriginalVoltage(vehicle.getBatteryVoltage().doubleValue());
        history.setOriginalCurrent(vehicle.getBatteryCurrent().doubleValue());
        
        return conversionHistoryRepository.save(history);
    }
    
    @Transactional
    public void createVehicleRegistrationInfos() {
        logger.info("Vehicle registration view automatically populates from underlying tables");
        
        entityManager.flush();
        entityManager.clear();
    }
    
    @Transactional
    public void seedSmallDataset() {
        clearDatabase();
        
        // createUser("admin", "admin", RoleType.ROLE_ADMIN);
        // createUser("user", "admin", RoleType.ROLE_USER);
        
        List<Vehicle> vehicles = new ArrayList<>();
        
        IntStream.range(1, 11).forEach(i -> {
            vehicles.add(vehicleRepository.save(createDieselVehicle(i)));
        });
        
        List<ElectricVehicle> electricVehicles = new ArrayList<>();
        IntStream.range(1, 11).forEach(i -> {
            ElectricVehicle vehicle = createElectricVehicle(i);
            electricVehicles.add((ElectricVehicle) vehicleRepository.save(vehicle));
        });
        
        IntStream.range(1, 11).forEach(i -> {
            vehicles.add(vehicleRepository.save(createGasVehicle(i)));
        });
        
        electricVehicles.stream()
            .filter(v -> v.getId() % 3 == 0)
            .forEach(this::createConversionHistory);
            
        createVehicleRegistrationInfos();
    }
    
    @Transactional
    public void seedLargeDataset(int count) {
        conversionHistoryRepository.deleteAll();
        vehicleRepository.deleteAll();
        
        int vehiclesPerType = count / 3;
        int remainder = count % 3;
        
        for (int i = 1; i <= vehiclesPerType; i++) {
            vehicleRepository.save(createDieselVehicle(i));
            
            if (i % 50 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        
        List<ElectricVehicle> electricVehicles = new ArrayList<>();
        for (int i = 1; i <= vehiclesPerType + remainder; i++) {
            ElectricVehicle vehicle = createElectricVehicle(i);
            electricVehicles.add((ElectricVehicle) vehicleRepository.save(vehicle));
            
            if (i % 50 == 0) {
                entityManager.flush();
                entityManager.clear();
                electricVehicles = electricVehicles.stream()
                    .filter(v -> v.getId() % 5 == 0)
                    .collect(Collectors.toList());
            }
        }
        
        for (int i = 1; i <= vehiclesPerType; i++) {
            vehicleRepository.save(createGasVehicle(i));
            
            if (i % 50 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        
        List<ElectricVehicle> convertibleVehicles = vehicleRepository.findAll().stream()
            .filter(v -> v instanceof ElectricVehicle && v.getId() % 5 == 0)
            .map(v -> (ElectricVehicle) v)
            .collect(Collectors.toList());
            
        for (ElectricVehicle vehicle : convertibleVehicles) {
            createConversionHistory(vehicle);
        }
        
        createVehicleRegistrationInfos();
    }
    
    @Transactional
    public void clearDatabase() {
        conversionHistoryRepository.deleteAll();
        vehicleRepository.deleteAll();
    }
    
    @Transactional
    public void clearAllData() {
        clearDatabase();
        userRepository.deleteAll();
    }
}
