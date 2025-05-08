package com.l1maor.vehicleworkshop.config;

import com.l1maor.vehicleworkshop.entity.Role;
import com.l1maor.vehicleworkshop.entity.User;
import com.l1maor.vehicleworkshop.repository.RoleRepository;
import com.l1maor.vehicleworkshop.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.HashSet;

@Configuration
public class DataInitializer {
    @Bean
    @Profile("dev")
    public CommandLineRunner initDevData(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        
        return args -> {
            if (roleRepository.count() == 0) {
                Role adminRole = new Role("ROLE_ADMIN");
                Role userRole = new Role("ROLE_USER");
                roleRepository.saveAll(Arrays.asList(adminRole, userRole));

                User adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setPasswordHash(passwordEncoder.encode("admin"));
                adminUser.setRoles(new HashSet<>(Arrays.asList(adminRole, userRole)));

                User regularUser = new User();
                regularUser.setUsername("user");
                regularUser.setPasswordHash(passwordEncoder.encode("user"));
                regularUser.setRoles(new HashSet<>(Arrays.asList(userRole)));
                
                userRepository.saveAll(Arrays.asList(adminUser, regularUser));
            }
        };
    }
}
