import {
  List,
  Datagrid,
  TextField,
  DeleteButton,
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
      <DeleteButton />
    </Datagrid>
  </List>
);
