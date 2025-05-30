CREATE OR REPLACE FUNCTION format_fuel_types(fuel_types_flags INTEGER)
RETURNS TEXT AS $$
DECLARE
    result TEXT := '';
BEGIN
    IF fuel_types_flags IS NULL OR fuel_types_flags = 0 THEN
        RETURN 'NONE';
    END IF;

    IF (fuel_types_flags & 1) > 0 THEN result := result || 'B83, '; END IF;
    IF (fuel_types_flags & 2) > 0 THEN result := result || 'B90, '; END IF;
    IF (fuel_types_flags & 4) > 0 THEN result := result || 'B94, '; END IF;
    IF (fuel_types_flags & 8) > 0 THEN result := result || 'B100, '; END IF;


    IF length(result) > 0 THEN
        RETURN LEFT(result, length(result) - 2);
    ELSE
        RETURN 'UNKNOWN';
    END IF;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION record_vehicle_conversion()
RETURNS TRIGGER AS $$
BEGIN

    IF (OLD.type = 'ELECTRIC' AND OLD.type != NEW.type) THEN
        DECLARE
            fuel_details TEXT := '';
        BEGIN

            IF NEW.type = 'GASOLINE' THEN
                fuel_details := ' with fuel types: ' || format_fuel_types(NEW.fuel_types_flags);
            END IF;


            INSERT INTO conversion_history (
                vehicle_id,
                conversion_date,
                original_battery_type,
                original_voltage,
                original_current,
                previous_vehicle_type,
                new_vehicle_type,
                conversion_details
            ) VALUES (
                OLD.id,
                CURRENT_TIMESTAMP,
                OLD.battery_type,
                OLD.battery_voltage,
                OLD.battery_current,
                OLD.type,
                NEW.type,
                'Converted from ' || OLD.type || ' to ' || NEW.type || fuel_details || ' on ' || CURRENT_TIMESTAMP
            );
        END;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'vehicle_conversion_trigger') THEN
        DROP TRIGGER vehicle_conversion_trigger ON vehicles;
    END IF;
END$$;

CREATE TRIGGER vehicle_conversion_trigger
AFTER UPDATE ON vehicles
FOR EACH ROW
EXECUTE FUNCTION record_vehicle_conversion();

DROP VIEW IF EXISTS vw_vehicle_registration_info CASCADE;
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
            format_fuel_types(v.fuel_types_flags)
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
