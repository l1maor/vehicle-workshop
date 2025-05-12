import { Admin, Resource, CustomRoutes } from "react-admin";
import { Layout } from "./Layout";
import { dataProvider } from "./providers/dataProvider";
import authProvider from "./providers/authProvider";
import { customRoutes } from "./routes";
import { Dashboard } from "./dashboard";
import { useCallback } from "react";

// Icons
import DirectionsCarIcon from "@mui/icons-material/DirectionsCar";
import PeopleIcon from "@mui/icons-material/People";
import VpnKeyIcon from "@mui/icons-material/VpnKey";

// Resources
import { VehicleList, VehicleCreate, VehicleEdit } from "./resources/vehicles";
import { UserList, UserCreate, UserEdit } from "./resources/users";
import { RoleList, RoleCreate, RoleEdit } from "./resources/roles";

export const App = () => {
  // Create custom login page redirecting unauthorized access to dashboard instead of logging out
  const handleAccessDenied = useCallback(() => {
    return Promise.resolve();
  }, []);
  
  return (
    <Admin 
      layout={Layout}
      dataProvider={dataProvider}
      authProvider={{...authProvider, handleAccessDenied}}
      dashboard={Dashboard}
      requireAuth
    >
      <CustomRoutes>
        {customRoutes}
      </CustomRoutes>
      {/* Always display vehicle resource */}
      <Resource
        name="vehicles"
        list={VehicleList}
        create={VehicleCreate}
        edit={VehicleEdit}
        icon={DirectionsCarIcon}
      />
      {/* Display users and roles only for admin users */}
      <Resource
        name="users"
        list={UserList}
        create={UserCreate}
        edit={UserEdit}
        icon={PeopleIcon}
        options={{ authRequired: true, authParams: { roles: ['ROLE_ADMIN'] } }}
      />
      <Resource
        name="roles"
        list={RoleList}
        create={RoleCreate}
        edit={RoleEdit}
        icon={VpnKeyIcon}
        options={{ authRequired: true, authParams: { roles: ['ROLE_ADMIN'] } }}
      />
    </Admin>
  );
};
