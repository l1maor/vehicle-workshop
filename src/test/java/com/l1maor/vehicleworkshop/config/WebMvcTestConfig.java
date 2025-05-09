package com.l1maor.vehicleworkshop.config;

import com.l1maor.vehicleworkshop.security.CustomUserDetailsService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

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
     * Password encoder for testing
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
