import { Menu as RaMenu } from "react-admin";
import DirectionsCarIcon from "@mui/icons-material/DirectionsCar";
import DashboardIcon from "@mui/icons-material/Dashboard";
import AssignmentIcon from "@mui/icons-material/Assignment";

type MenuProps = {
  dense?: boolean;
};

export const Menu = ({ dense = false }: MenuProps) => {
  return (
    <RaMenu dense={dense}>
      <RaMenu.DashboardItem
        to="/"
        primaryText="Dashboard"
        leftIcon={<DashboardIcon />}
      />
      <RaMenu.Item
        to="/vehicles"
        primaryText="Vehicles"
        leftIcon={<DirectionsCarIcon />}
      />
      <RaMenu.Item
        to="/vehicles/registrations"
        primaryText="Vehicle Registrations"
        leftIcon={<AssignmentIcon />}
      />
    </RaMenu>
  );
};
