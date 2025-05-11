import {
  List,
  Datagrid,
  TextField,
  EditButton,
  DeleteButton,
  TextInput,
  ArrayField,
  SingleFieldList,
  ChipField,
} from 'react-admin';

const userFilters = [
  <TextInput source="username" label="Search" alwaysOn />,
];

export const UserList = () => (
  <List filters={userFilters}>
    <Datagrid>
      <TextField source="id" />
      <TextField source="username" />
      <ArrayField source="roles">
        <SingleFieldList>
          <ChipField source="" />
        </SingleFieldList>
      </ArrayField>
      <EditButton />
      <DeleteButton />
    </Datagrid>
  </List>
);
