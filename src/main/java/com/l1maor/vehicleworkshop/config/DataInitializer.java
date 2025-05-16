package com.l1maor.vehicleworkshop.config;

import com.l1maor.vehicleworkshop.entity.RoleType;
import com.l1maor.vehicleworkshop.entity.User;
import com.l1maor.vehicleworkshop.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;

@Configuration
public class DataInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    @Bean
    public CommandLineRunner initDevData(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        
        logger.info("Initializing development data bean");
        
        return args -> {
            logger.info("Checking if default users need to be created");
            if (userRepository.count() == 0) {
                logger.info("No users found in database, creating default admin and regular users");
                
                User adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setPasswordHash(passwordEncoder.encode("admin"));
                adminUser.setRoleType(RoleType.ROLE_ADMIN);
                logger.debug("Created admin user with username: {}", adminUser.getUsername());

                User regularUser = new User();
                regularUser.setUsername("user");
                regularUser.setPasswordHash(passwordEncoder.encode("user"));
                regularUser.setRoleType(RoleType.ROLE_USER);
                logger.debug("Created regular user with username: {}", regularUser.getUsername());
                
                userRepository.saveAll(Arrays.asList(adminUser, regularUser));
                logger.info("Default users created successfully");
            } else {
                logger.info("Users already exist in database, skipping default user creation");
            }
        };
    }
}
