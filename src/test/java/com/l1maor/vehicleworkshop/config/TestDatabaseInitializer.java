package com.l1maor.vehicleworkshop.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@Profile("test")
public class TestDatabaseInitializer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initializeDatabase() {
        try {
            jdbcTemplate.execute("DROP VIEW IF EXISTS vehicle_registration_info");
            System.out.println("Successfully dropped the vehicle_registration_info view if it existed");
            
            jdbcTemplate.execute("""
                CREATE VIEW vehicle_registration_info AS
                SELECT
                    v.id,
                    v.type as type,
                    CASE
                        WHEN v.type = 'DIESEL' THEN 
                            v.license_plate || ' + ' || COALESCE(v.injection_pump_type, 'UNKNOWN')
                        WHEN v.type = 'ELECTRIC' THEN 
                            v.vin || ' + ' || v.battery_voltage || 'V + ' || 
                            v.battery_current || 'A + ' || COALESCE(v.battery_type, 'UNKNOWN')
                        WHEN v.type = 'GASOLINE' THEN 
                            v.license_plate || ' + FUELS: ' || 
                            COALESCE(
                                (SELECT string_agg(CAST(fuel_types AS text), ', ') 
                                 FROM gas_vehicle_fuel_types 
                                 WHERE gas_vehicle_id = v.id),
                                'STANDARD'
                            )
                        ELSE 
                            'Unknown vehicle type'
                    END AS registration_info,
                    CASE
                        WHEN v.type = 'ELECTRIC' THEN true
                        ELSE false
                    END AS is_convertible,
                    CASE
                        WHEN v.type = 'ELECTRIC' THEN 
                            v.license_plate || ' + POTENTIAL FUELS: B83, B90, B94, B100'
                        ELSE NULL
                    END AS conversion_data
                FROM vehicles v
            """);
            System.out.println("Successfully created the vehicle_registration_info view for tests");
        } catch (Exception e) {
            System.err.println("Failed to create vehicle_registration_info view: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
