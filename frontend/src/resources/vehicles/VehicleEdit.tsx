import {
  Edit,
  SimpleForm,
  TextInput,
  SelectInput,
  ArrayInput,
  SimpleFormIterator,
  NumberInput,
  FormDataConsumer,
  useNotify,
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
  console.log("RENDER :: VehicleEdit");
  const notify = useNotify();

  const onSuccess = () => {
    notify('Vehicle updated successfully');
  };
  
  return (
    <Edit mutationOptions={{ onSuccess }}>
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
                </>
              );
            }

            if (formData.type === "GASOLINE") {
              return (
                <ArrayInput source="fuelTypes" validate={fuelTypesValidator}>
                  <SimpleFormIterator>
                    <SelectInput
                      source="id"
                      choices={[
                        { id: "B83", name: "B83" },
                        { id: "B90", name: "B90" },
                        { id: "B94", name: "B94" },
                        { id: "B100", name: "B100" },
                      ]}
                    />
                  </SimpleFormIterator>
                </ArrayInput>
              );
            }

            return null;
          }}
        </FormDataConsumer>
      </SimpleForm>
    </Edit>
  );
};
