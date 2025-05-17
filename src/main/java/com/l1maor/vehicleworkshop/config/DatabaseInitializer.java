package com.l1maor.vehicleworkshop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class DatabaseInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        logger.info("Initializing database objects (triggers, views, functions)...");

        try {
            Boolean viewExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT FROM information_schema.views " +
                "WHERE table_schema = 'public' AND table_name = 'vw_vehicle_registration_info')",
                Boolean.class);

            logger.info("View vw_vehicle_registration_info exists before initialization: {}", viewExists);


            ClassPathResource resource = new ClassPathResource("sql/db-objects.sql");
            String sql;
            try (BufferedReader reader = new BufferedReader(
                 new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                sql = reader.lines().collect(Collectors.joining("\n"));
            }

            jdbcTemplate.execute(sql);

            Boolean viewCreated = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT FROM information_schema.views " +
                "WHERE table_schema = 'public' AND table_name = 'vw_vehicle_registration_info')",
                Boolean.class);

            logger.info("View vw_vehicle_registration_info exists after initialization: {}", viewCreated);

            Integer viewRowCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM vw_vehicle_registration_info",
                Integer.class);

            logger.info("View vw_vehicle_registration_info contains {} rows", viewRowCount != null ? viewRowCount : 0);

            if (verifyDatabaseObjects()) {
                logger.info("Database objects successfully initialized and verified.");
            } else {
                logger.warn("Database objects may not have been initialized correctly. Check database logs.");
            }
        } catch (Exception e) {
            logger.error("Error initializing database objects", e);
        }
    }


    private boolean verifyDatabaseObjects() {
        try {
            Integer viewCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.views " +
                "WHERE table_schema = 'public' AND table_name = 'vw_vehicle_registration_info'",
                Integer.class);

            Integer triggerCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_trigger WHERE tgname = 'vehicle_conversion_trigger'",
                Integer.class);

            Integer functionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_proc WHERE proname = 'record_vehicle_conversion'",
                Integer.class);

            boolean viewExists = viewCount != null && viewCount > 0;
            boolean triggerExists = triggerCount != null && triggerCount > 0;
            boolean functionExists = functionCount != null && functionCount > 0;

            logger.info("Database objects verification: view={}, trigger={}, function={}",
                       viewExists, triggerExists, functionExists);

            return viewExists && triggerExists && functionExists;
        } catch (Exception e) {
            logger.error("Error verifying database objects", e);
            return false;
        }
    }
}
