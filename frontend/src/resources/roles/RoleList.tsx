import {
  List,
  Datagrid,
  TextField,
  DeleteButton,
  EditButton,
  TextInput,
} from 'react-admin';

const roleFilters = [
  <TextInput source="name" label="Search" alwaysOn />,
];

export const RoleList = () => (
  <List filters={roleFilters}>
    <Datagrid>
      <TextField source="id" />
      <TextField source="name" />
      <EditButton />
      <DeleteButton />
    </Datagrid>
  </List>
);
