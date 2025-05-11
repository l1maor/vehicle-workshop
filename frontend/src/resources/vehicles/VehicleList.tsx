import {
  List,
  Datagrid,
  TextField,
  EditButton,
  DeleteButton,
  TextInput,
  SelectInput,
  FunctionField,
  Button,
  useRecordContext,
  FilterButton,
  FilterForm,
  SearchInput,
  TopToolbar,
} from 'react-admin';
import { Link } from 'react-router-dom';
import VisibilityIcon from '@mui/icons-material/Visibility';
import ElectricCarIcon from '@mui/icons-material/ElectricCar';

const VehicleFilters = [
  <SearchInput source="q" alwaysOn />,
  <SelectInput
    source="type"
    choices={[
      { id: 'DIESEL', name: 'Diesel' },
      { id: 'ELECTRIC', name: 'Electric' },
      { id: 'GASOLINE', name: 'Gasoline' },
    ]}
  />,
];

const ListActions = () => (
  <TopToolbar>
    <FilterButton />
  </TopToolbar>
);

const ConvertToGasButton = () => {
  const record = useRecordContext();
  
  if (!record || record.type !== 'ELECTRIC') return null;
  
  return (
    <Button
      component={Link}
      to={`/vehicles/${record.id}/convert`}
      label="Convert to Gas"
      startIcon={<ElectricCarIcon />}
    />
  );
};

const RegistrationButton = () => {
  const record = useRecordContext();
  if (!record) return null;
  
  return (
    <Button
      component={Link}
      to={`/vehicles/${record.id}/registration`}
      label="Registration"
      startIcon={<VisibilityIcon />}
    />
  );
};

export const VehicleList = () => (
  <List filters={VehicleFilters} actions={<ListActions />}>
    <Datagrid>
      <TextField source="id" />
      <TextField source="vin" label="VIN" />
      <TextField source="licensePlate" label="License Plate" />
      <TextField source="type" />
      <FunctionField
        label="Specific Details"
        render={(record: any) => {
          if (record.type === 'DIESEL') {
            return `Injection Pump: ${record.injectionPumpType || 'N/A'}`;
          } else if (record.type === 'ELECTRIC') {
            return `Battery: ${record.batteryType || 'N/A'}, Voltage: ${record.batteryVoltage || 'N/A'}, Current: ${record.batteryCurrent || 'N/A'}`;
          } else if (record.type === 'GASOLINE') {
            return `Fuel Types: ${record.fuelTypes ? record.fuelTypes.join(', ') : 'N/A'}`;
          }
          return 'N/A';
        }}
      />
      <ConvertToGasButton />
      <RegistrationButton />
      <EditButton />
      <DeleteButton />
    </Datagrid>
  </List>
);
