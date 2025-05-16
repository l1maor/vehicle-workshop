import {
  Create,
  SimpleForm,
  TextInput,
  SelectInput,
  ArrayInput,
  SimpleFormIterator,
  NumberInput,
  FormDataConsumer,
  useNotify,
  useRedirect,
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

export const VehicleCreate = () => {
  const notify = useNotify();
  const redirect = useRedirect();

  const onSuccess = () => {
    notify('Vehicle created successfully');
    redirect('list', 'vehicles');
  };
  
  return (
  <Create mutationOptions={{ onSuccess }}>
    <SimpleForm validate={validateVehicleFields}>
      <ErrorDisplay />
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
      />

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
  </Create>
  );
};
