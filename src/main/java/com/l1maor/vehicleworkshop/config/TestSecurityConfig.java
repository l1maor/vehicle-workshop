package com.l1maor.vehicleworkshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class TestSecurityConfig {
    
    @Bean
    @Order(1) // This will be evaluated before the main security config
    public SecurityFilterChain testEndpointsFilterChain(HttpSecurity http) throws Exception {
        return http
            .securityMatcher("/api/test/**") // /api/test/** temporarily unsecured to easily seed the db
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/test/**").permitAll()
            )
            .build();
    }
}
