import { Admin, Resource, CustomRoutes } from "react-admin";
import { Layout } from "./Layout";
import { dataProvider } from "./providers/dataProvider";
import authProvider from "./providers/authProvider";
import { customRoutes } from "./routes";
import { Dashboard } from "./dashboard";
import { useCallback } from "react";

import DirectionsCarIcon from "@mui/icons-material/DirectionsCar";

import { VehicleList, VehicleCreate, VehicleEdit } from "./resources/vehicles";

export const App = () => {
  const handleAccessDenied = useCallback(() => {
    return Promise.resolve();
  }, []);

  return (
    <Admin
      layout={Layout}
      dataProvider={dataProvider}
      authProvider={{ ...authProvider, handleAccessDenied }}
      dashboard={Dashboard}
      requireAuth
    >
      <CustomRoutes>{customRoutes}</CustomRoutes>
      <Resource
        name="vehicles"
        list={VehicleList}
        create={VehicleCreate}
        edit={VehicleEdit}
        icon={DirectionsCarIcon}
      />
    </Admin>
  );
};
