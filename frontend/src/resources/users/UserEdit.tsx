import {
  Edit,
  SimpleForm,
  TextInput,
  required,
  PasswordInput,
  SelectArrayInput,
} from 'react-admin';

export const UserEdit = () => (
  <Edit>
    <SimpleForm>
      <TextInput source="id" disabled />
      <TextInput source="username" validate={required()} />
      <PasswordInput source="password" />
      <SelectArrayInput
        source="roles"
        choices={[
          { id: 'ROLE_ADMIN', name: 'Admin' },
          { id: 'ROLE_USER', name: 'User' },
        ]}
        validate={required()}
      />
    </SimpleForm>
  </Edit>
);
