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

            dropVehicleRegistrationInfoTable();


            ClassPathResource resource = new ClassPathResource("sql/db-objects.sql");
            String sql;
            try (BufferedReader reader = new BufferedReader(
                 new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                sql = reader.lines().collect(Collectors.joining("\n"));
            }


            jdbcTemplate.execute(sql);


            if (verifyDatabaseObjects()) {
                logger.info("Database objects successfully initialized and verified.");
            } else {
                logger.warn("Database objects may not have been initialized correctly. Check database logs.");
            }
        } catch (Exception e) {
            logger.error("Error initializing database objects", e);
        }
    }

    private void dropVehicleRegistrationInfoTable() {
        try {

            Boolean isTable = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT FROM information_schema.tables " +
                "WHERE table_schema = 'public' AND table_name = 'vehicle_registration_info')",
                Boolean.class);


            Boolean isVwTable = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT FROM information_schema.tables " +
                "WHERE table_schema = 'public' AND table_name = 'vw_vehicle_registration_info')",
                Boolean.class);

            if (Boolean.TRUE.equals(isTable)) {
                logger.info("Dropping vehicle_registration_info table to replace it with a view");
                jdbcTemplate.execute("DROP TABLE IF EXISTS vehicle_registration_info CASCADE");
            }

            if (Boolean.TRUE.equals(isVwTable)) {
                logger.info("Dropping vw_vehicle_registration_info table to replace it with a view");
                jdbcTemplate.execute("DROP TABLE IF EXISTS vw_vehicle_registration_info CASCADE");
            }
        } catch (Exception e) {
            logger.error("Error dropping vehicle_registration_info table", e);
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
