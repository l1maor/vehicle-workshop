import {
  Edit,
  SimpleForm,
  TextInput,
  SelectInput,
  required,
  ArrayInput,
  SimpleFormIterator,
  NumberInput,
  FormDataConsumer
} from 'react-admin';

export const VehicleEdit = () => {
  console.log('RENDER :: VehicleEdit');
  return (
  <Edit>
    <SimpleForm>
      <TextInput source="id" disabled />
      <TextInput source="vin" validate={required()} />
      <TextInput source="licensePlate" validate={required()} />
      <SelectInput
        source="type"
        validate={required()}
        choices={[
          { id: 'DIESEL', name: 'Diesel' },
          { id: 'ELECTRIC', name: 'Electric' },
          { id: 'GASOLINE', name: 'Gasoline' },
        ]}
        disabled
      />
      <TextInput source="version" disabled />
      
      <FormDataConsumer>
        {({ formData, ...rest }) => {
          if (formData.type === 'DIESEL') {
            return (
              <SelectInput
                source="injectionPumpType"
                choices={[
                  { id: 'LINEAR', name: 'Linear' },
                  { id: 'ROTARY', name: 'Rotary' },
                ]}
                validate={required()}
                {...rest}
              />
            );
          }
          
          if (formData.type === 'ELECTRIC') {
            return (
              <>
                <SelectInput
                  source="batteryType"
                  choices={[
                    { id: 'GEL', name: 'Gel' },
                    { id: 'LITHIUM', name: 'Lithium' },
                  ]}
                  validate={required()}
                  {...rest}
                />
                <NumberInput 
                  source="batteryVoltage" 
                  validate={required()}
                  {...rest}
                />
                <NumberInput 
                  source="batteryCurrent" 
                  validate={required()}
                  {...rest}
                />
              </>
            );
          }
          
          if (formData.type === 'GASOLINE') {
            return (
              <ArrayInput source="fuelTypes" validate={required()}>
                <SimpleFormIterator>
                  <SelectInput
                    choices={[
                      { id: 'B83', name: 'B83' },
                      { id: 'B90', name: 'B90' },
                      { id: 'B94', name: 'B94' },
                      { id: 'B100', name: 'B100' },
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
}
