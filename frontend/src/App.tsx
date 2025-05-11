import { Admin, Resource } from "react-admin";
import { Layout } from "./Layout";
import { dataProvider } from "./providers/dataProvider";
import authProvider from "./providers/authProvider";
import { customRoutes } from "./routes";
import { Dashboard } from "./dashboard";

// Icons
import DirectionsCarIcon from "@mui/icons-material/DirectionsCar";
import PeopleIcon from "@mui/icons-material/People";
import VpnKeyIcon from "@mui/icons-material/VpnKey";

// Resources
import { VehicleList, VehicleCreate, VehicleEdit } from "./resources/vehicles";
import { UserList, UserCreate, UserEdit } from "./resources/users";
import { RoleList, RoleCreate } from "./resources/roles";

export const App = () => (
  <Admin 
    layout={Layout}
    dataProvider={dataProvider}
    authProvider={authProvider}
    dashboard={Dashboard}
  >
    {customRoutes}
    <Resource
      name="vehicles"
      list={VehicleList}
      create={VehicleCreate}
      edit={VehicleEdit}
      icon={DirectionsCarIcon}
    />
    <Resource
      name="users"
      list={UserList}
      create={UserCreate}
      edit={UserEdit}
      icon={PeopleIcon}
    />
    <Resource
      name="roles"
      list={RoleList}
      create={RoleCreate}
      icon={VpnKeyIcon}
    />
  </Admin>
);
