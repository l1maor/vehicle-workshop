import {
  Edit,
  SimpleForm,
  TextInput,
  SelectInput,
  SelectArrayInput,
  NumberInput,
  FormDataConsumer,
  useNotify,
  BooleanInput,
} from "react-admin";

import {
  vinValidator,
  licensePlateValidator,
  batteryVoltageValidator,
  batteryCurrentValidator,
  requiredSelect,
  fuelTypesValidator
} from "../../validators/validators";

import { validateVehicleFields } from "../../validators/crossFieldValidators";
import { ErrorDisplay } from "../../components/ErrorDisplay";



export const VehicleEdit = () => {

  const notify = useNotify();

  const transform = (data: any) => {

    
    const transformedData = { ...data };
    
    if (transformedData.type === 'GASOLINE' && transformedData.fuelTypes) {
      if (!Array.isArray(transformedData.fuelTypes)) {

        transformedData.fuelTypes = transformedData.fuelTypes ? [transformedData.fuelTypes] : [];
      }
    }
    

    return transformedData;
  };
  
  return (
    <Edit
      mutationMode="pessimistic"
      transform={transform}
      mutationOptions={{
        onSuccess: (data) => {

          notify('Vehicle updated successfully', { type: 'success' });
        },
        onError: (error) => {

          notify(`Error: ${error.message || 'Failed to update vehicle'}`, { type: 'error' });
        },
      }}
    >  
      <SimpleForm validate={validateVehicleFields}>
        <ErrorDisplay />
        <TextInput source="id" disabled />
        <TextInput source="vin" validate={vinValidator} />
        <TextInput source="licensePlate" validate={licensePlateValidator} />
        <SelectInput
          source="type"
          validate={requiredSelect}
          choices={[
            { id: "DIESEL", name: "Diesel" },
            { id: "ELECTRIC", name: "Electric" },
            { id: "GASOLINE", name: "Gasoline" },
          ]}
          disabled
        />
        <TextInput source="version" disabled />

        <FormDataConsumer>
          {({ formData, ...rest }) => {
            if (formData.type === "DIESEL") {
              return (
                <SelectInput
                  source="injectionPumpType"
                  choices={[
                    { id: "LINEAR", name: "Linear" },
                    { id: "ROTARY", name: "Rotary" },
                  ]}
                  validate={requiredSelect}
                  {...rest}
                />
              );
            }

            if (formData.type === "ELECTRIC") {
              return (
                <>
                  <SelectInput
                    source="batteryType"
                    choices={[
                      { id: "GEL", name: "Gel" },
                      { id: "LITHIUM", name: "Lithium" },
                    ]}
                    validate={requiredSelect}
                    {...rest}
                  />
                  <NumberInput
                    source="batteryVoltage"
                    validate={batteryVoltageValidator}
                    {...rest}
                  />
                  <NumberInput
                    source="batteryCurrent"
                    validate={batteryCurrentValidator}
                    {...rest}
                  />
                  <BooleanInput
                    source="convertible"
                    label="Convertible"
                    defaultValue={false}
                    {...rest}
                  />
                </>
              );
            }

            if (formData.type === "GASOLINE") {
              return (
                <SelectArrayInput
                  source="fuelTypes"
                  validate={fuelTypesValidator}
                  choices={[
                    { id: "B83", name: "B83" },
                    { id: "B90", name: "B90" },
                    { id: "B94", name: "B94" },
                    { id: "B100", name: "B100" },
                  ]}
                  parse={(value) => {

                    return value;
                  }}
                  format={(value) => {

                    return value;
                  }}
                />                  
              );
            }

            return null;
          }}
        </FormDataConsumer>
      </SimpleForm>
    </Edit>
  );
};
