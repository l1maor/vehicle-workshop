import {
  List,
  Datagrid,
  TextField,
  EditButton,
  DeleteButton,
  SelectInput,
  FunctionField,
  Button,
  useRecordContext,
  FilterButton,
  SearchInput,
  TopToolbar,
  CreateButton,
  useRedirect,
} from 'react-admin';

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
    <CreateButton />
  </TopToolbar>
);

const ConvertToGasButton = () => {
  const record = useRecordContext();
  const redirect = useRedirect();
  
  if (!record || record.type !== 'ELECTRIC') return null;
  
  const handleClick = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    redirect(`/vehicles/${record.id}/convert`);
  };
  
  return (
    <Button
      onClick={handleClick}
      label="Convert to Gas"
      startIcon={<ElectricCarIcon />}
    />
  );
};

const RegistrationButton = () => {
  const record = useRecordContext();
  const redirect = useRedirect();
  if (!record) return null;
  
  const handleClick = () => {
    redirect(`/vehicles/${record.id}/registration`);
  };
  
  return (
    <Button
      onClick={handleClick}
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
