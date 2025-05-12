import * as React from 'react';
import { useState } from 'react';
import { useMediaQuery, Theme } from '@mui/material';
import { Menu as RaMenu, usePermissions } from 'react-admin';
import DirectionsCarIcon from "@mui/icons-material/DirectionsCar";
import PeopleIcon from "@mui/icons-material/People";
import VpnKeyIcon from "@mui/icons-material/VpnKey";
import DashboardIcon from "@mui/icons-material/Dashboard";

export const Menu = ({ dense = false }) => {
  const [state, setState] = useState({
    menuCatalog: false,
    menuSales: false,
    menuCustomers: false,
  });
  const isXSmall = useMediaQuery((theme: Theme) =>
    theme.breakpoints.down('sm')
  );
  const { permissions } = usePermissions();
  const isAdmin = Array.isArray(permissions) && permissions.includes('ROLE_ADMIN');

  return (
    <RaMenu dense={dense}>
      <RaMenu.DashboardItem to="/" primaryText="Dashboard" leftIcon={<DashboardIcon />} />
      <RaMenu.Item
        to="/vehicles"
        primaryText="Vehicles"
        leftIcon={<DirectionsCarIcon />}
      />
      {isAdmin && (
        <>
          <RaMenu.Item
            to="/users"
            primaryText="Users"
            leftIcon={<PeopleIcon />}
          />
          <RaMenu.Item
            to="/roles"
            primaryText="Roles"
            leftIcon={<VpnKeyIcon />}
          />
        </>
      )}
    </RaMenu>
  );
};
