package com.l1maor.vehicleworkshop.config;

import com.l1maor.vehicleworkshop.repository.UserRepository;
import com.l1maor.vehicleworkshop.repository.VehicleRepository;
import com.l1maor.vehicleworkshop.repository.ConversionHistoryRepository;
import com.l1maor.vehicleworkshop.security.CustomUserDetailsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;

import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Test configuration for WebMvcTest
 * This configuration provides mock beans for dependencies that are required
 * by WebMvcTest but are not directly tested
 */
@TestConfiguration
public class WebMvcTestConfig {

    /**
     * Mock user details service for authentication
     */
    @MockBean
    private CustomUserDetailsService userDetailsService;

    /**
     * Mock JPA components
     */
    @MockBean
    private EntityManager entityManager;
    
    @MockBean
    private EntityManagerFactory entityManagerFactory;

    /**
     * Mock repositories
     */
    @MockBean
    private VehicleRepository vehicleRepository;

    @MockBean
    private ConversionHistoryRepository conversionHistoryRepository;
    
    @MockBean
    private UserRepository userRepository;
    


    /**
     * Password encoder for testing
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Application event multicaster for testing
     */
    @Bean
    public ApplicationEventMulticaster applicationEventMulticaster() {
        return new SimpleApplicationEventMulticaster();
    }
    
    /**
     * Transaction manager for testing
     */
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
