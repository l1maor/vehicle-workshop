/**
 * Validates vehicle fields based on the vehicle type
 * 
 * @param values Form values
 * @returns An object with validation errors, or empty if no errors
 */
export const validateVehicleFields = (values: any) => {
  const errors: Record<string, any> = {};

  if (!values.type) {
    return errors; // Basic required validation will handle this
  }

  switch (values.type) {
    case 'DIESEL':
      if (!values.injectionPumpType) {
        errors.injectionPumpType = 'Injection pump type is required for diesel vehicles';
      }
      break;

    case 'ELECTRIC':
      if (!values.batteryType) {
        errors.batteryType = 'Battery type is required for electric vehicles';
      }

      if (values.batteryVoltage === undefined || values.batteryVoltage === null) {
        errors.batteryVoltage = 'Battery voltage is required for electric vehicles';
      } else if (parseInt(values.batteryVoltage) < 0 || parseInt(values.batteryVoltage) > 1000) {
        errors.batteryVoltage = 'Battery voltage must be between 0 and 1000';
      }

      if (values.batteryCurrent === undefined || values.batteryCurrent === null) {
        errors.batteryCurrent = 'Battery current is required for electric vehicles';
      } else if (parseInt(values.batteryCurrent) < 0 || parseInt(values.batteryCurrent) > 1000) {
        errors.batteryCurrent = 'Battery current must be between 0 and 1000';
      }
      break;

    case 'GASOLINE':
      if (!values.fuelTypes || values.fuelTypes.length === 0) {
        errors.fuelTypes = 'At least one fuel type is required for gasoline vehicles';
      }
      break;
  }

  return errors;
};
