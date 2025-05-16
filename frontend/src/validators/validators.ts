import { required as raRequired, minValue, maxValue, regex } from 'react-admin';

// VIN validation - 17 characters, alphanumeric, excluding I, O, Q
export const vinValidator = [
  raRequired(),
  (value: string) => 
    value && value.length !== 17 
      ? 'VIN must be exactly 17 characters' 
      : undefined,
  regex(/^[A-HJ-NPR-Z0-9]{17}$/, 'VIN format is invalid')
];

// License plate validation - non-empty, maximum 20 characters
export const licensePlateValidator = [
  raRequired(),
  (value: string) => 
    value && value.length > 20 
      ? 'License plate cannot exceed 20 characters' 
      : undefined
];

// Number field validators
export const positiveNumberValidator = [
  minValue(0, 'Value cannot be negative')
];

export const batteryVoltageValidator = [
  raRequired('Battery voltage is required'),
  ...positiveNumberValidator,
  maxValue(1000, 'Battery voltage cannot exceed 1000V')
];

export const batteryCurrentValidator = [
  raRequired('Battery current is required'),
  ...positiveNumberValidator,
  maxValue(1000, 'Battery current cannot exceed 1000A')
];

// Required validator for select fields
export const requiredSelect = raRequired('This field is required');

// For fuel types array validation
export const minArrayLength = (min: number, message = `At least ${min} item is required`) => 
  (value: any[]) => value && value.length < min ? message : undefined;

export const fuelTypesValidator = [
  raRequired('At least one fuel type is required'),
  (value: any[]) => value && value.length === 0 ? 'At least one fuel type is required' : undefined
];
