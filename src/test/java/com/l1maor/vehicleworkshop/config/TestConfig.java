package com.l1maor.vehicleworkshop.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Test configuration for repository tests
 * This configuration provides beans required by the test context
 */
@TestConfiguration
public class TestConfig {
    
    /**
     * Password encoder bean for test context
     * Marked as Primary to ensure it's selected when multiple definitions exist
     */
    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
