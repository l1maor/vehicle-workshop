package com.l1maor.vehicleworkshop.data;

import com.l1maor.vehicleworkshop.config.TestConfig;
import com.l1maor.vehicleworkshop.entity.*;
import com.l1maor.vehicleworkshop.repository.ConversionHistoryRepository;
import com.l1maor.vehicleworkshop.repository.RoleRepository;
import com.l1maor.vehicleworkshop.repository.UserRepository;
import com.l1maor.vehicleworkshop.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Utility class to generate test data for tests
 */
@Component
@Import(TestConfig.class)
public class TestDataGenerator {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private ConversionHistoryRepository conversionHistoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Generates a random diesel vehicle with unique VIN and license plate
     */
    public DieselVehicle createDieselVehicle() {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        DieselVehicle vehicle = new DieselVehicle();
        vehicle.setVin("DIESEL" + uuid);
        vehicle.setLicensePlate("D-" + uuid);
        vehicle.setInjectionPumpType(getRandomEnum(InjectionPumpType.class));
        return vehicle;
    }

    /**
     * Generates a random electric vehicle with unique VIN and license plate
     */
    public ElectricVehicle createElectricVehicle() {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        ElectricVehicle vehicle = new ElectricVehicle();
        vehicle.setVin("ELECTRIC" + uuid);
        vehicle.setLicensePlate("E-" + uuid);
        vehicle.setBatteryType(getRandomEnum(BatteryType.class));
        vehicle.setBatteryVoltage(new Random().nextDouble() * 400 + 100); // 100-500V
        vehicle.setBatteryCurrent(new Random().nextDouble() * 100 + 50);  // 50-150A
        return vehicle;
    }

    /**
     * Generates a random gasoline vehicle with unique VIN and license plate
     */
    public GasVehicle createGasVehicle() {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        GasVehicle vehicle = new GasVehicle();
        vehicle.setVin("GAS" + uuid);
        vehicle.setLicensePlate("G-" + uuid);
        
        // Random selection of 1-4 fuel types
        Set<FuelType> fuelTypes = new HashSet<>();
        List<FuelType> allTypes = Arrays.asList(FuelType.values());
        Collections.shuffle(allTypes);
        int numTypes = new Random().nextInt(allTypes.size()) + 1;
        for (int i = 0; i < numTypes; i++) {
            fuelTypes.add(allTypes.get(i));
        }
        vehicle.setFuelTypes(fuelTypes);
        
        return vehicle;
    }

    /**
     * Creates a role with the given name if it doesn't exist
     */
    @Transactional
    public Role createRoleIfNotExists(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    Role role = new Role(name);
                    return roleRepository.save(role);
                });
    }

    /**
     * Creates a user with the given username and roles
     */
    @Transactional
    public User createUser(String username, String password, String... roleNames) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            roles.add(createRoleIfNotExists(roleName));
        }
        user.setRoles(roles);
        
        return userRepository.save(user);
    }
    
    /**
     * Seeds the database with sample data
     */
    @Transactional
    public void seedDatabase() {
        // Create roles
        Role adminRole = createRoleIfNotExists("ROLE_ADMIN");
        Role userRole = createRoleIfNotExists("ROLE_USER");
        Role mechanicRole = createRoleIfNotExists("ROLE_MECHANIC");
        
        // Create users
        createUser("admin", "admin123", "ROLE_ADMIN");
        createUser("user", "user123", "ROLE_USER");
        createUser("mechanic", "mech123", "ROLE_MECHANIC");
        
        // Create vehicles
        for (int i = 0; i < 10; i++) {
            vehicleRepository.save(createDieselVehicle());
            vehicleRepository.save(createElectricVehicle());
            vehicleRepository.save(createGasVehicle());
        }
    }
    
    /**
     * Clear all data from the database
     */
    @Transactional
    public void clearDatabase() {
        // Delete conversion history records first since they reference vehicles
        conversionHistoryRepository.deleteAll();
        // Now we can safely delete vehicles
        vehicleRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }
    
    /**
     * Utility method to get a random enum value
     */
    private <T extends Enum<?>> T getRandomEnum(Class<T> enumClass) {
        T[] values = enumClass.getEnumConstants();
        return values[new Random().nextInt(values.length)];
    }
}
