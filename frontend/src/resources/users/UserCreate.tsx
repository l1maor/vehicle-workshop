import {
  Create,
  SimpleForm,
  TextInput,
  required,
  PasswordInput,
  SelectArrayInput,
} from 'react-admin';

export const UserCreate = () => (
  <Create>
    <SimpleForm>
      <TextInput source="username" validate={required()} />
      <PasswordInput source="password" validate={required()} />
      <SelectArrayInput
        source="roles"
        choices={[
          { id: 'ROLE_ADMIN', name: 'Admin' },
          { id: 'ROLE_USER', name: 'User' },
        ]}
        validate={required()}
      />
    </SimpleForm>
  </Create>
);
