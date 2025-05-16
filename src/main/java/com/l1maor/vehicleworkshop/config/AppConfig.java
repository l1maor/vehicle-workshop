package com.l1maor.vehicleworkshop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    @Bean
    public PasswordEncoder passwordEncoder() {
        logger.debug("Creating BCrypt password encoder bean");
        return new BCryptPasswordEncoder();
    }
}
