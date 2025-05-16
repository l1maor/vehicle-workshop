CREATE OR REPLACE VIEW vw_vehicle_registration_info AS
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
            CASE
                WHEN v.fuel_types_flags IS NULL THEN 'UNKNOWN'
                WHEN v.fuel_types_flags = 0 THEN 'NONE'
                ELSE
                    CASE WHEN (v.fuel_types_flags & 1) > 0 THEN 'B83, ' ELSE '' END ||
                    CASE WHEN (v.fuel_types_flags & 2) > 0 THEN 'B90, ' ELSE '' END ||
                    CASE WHEN (v.fuel_types_flags & 4) > 0 THEN 'B94, ' ELSE '' END ||
                    CASE WHEN (v.fuel_types_flags & 8) > 0 THEN 'B100' ELSE '' END
            END
        ELSE 
            'Unknown vehicle type'
    END AS registration_info,

    COALESCE(v.convertible, 
      CASE
        WHEN v.type = 'ELECTRIC' THEN true
        ELSE false
      END
    ) AS is_convertible,

    CASE
        WHEN v.type = 'ELECTRIC' AND v.convertible = true THEN 
            v.license_plate || ' + POTENTIAL FUELS: B83, B90, B94, B100'
        ELSE NULL
    END AS conversion_data
FROM vehicles v;
