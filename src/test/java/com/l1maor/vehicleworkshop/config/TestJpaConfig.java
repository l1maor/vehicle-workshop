package com.l1maor.vehicleworkshop.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Test JPA configuration that disables JPA auditing for tests
 */
@TestConfiguration
@EnableJpaRepositories(basePackages = "com.l1maor.vehicleworkshop.repository")
public class TestJpaConfig {
    // Empty configuration class to override the auditing
}
