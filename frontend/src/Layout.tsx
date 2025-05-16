import type { ReactNode } from "react";
import {
  Layout as RALayout,
  AppBar,
  CheckForApplicationUpdate,
} from "react-admin";
import { Typography, Box } from "@mui/material";
import { Menu } from "./Menu";

const CustomAppBar = () => {
  return (
    <AppBar color="primary">
      <Box flex="1">
        <Typography variant="h6" id="react-admin-title">
          Vehicle Workshop Management
        </Typography>
      </Box>
    </AppBar>
  );
};

export const Layout = ({ children }: { children: ReactNode }) => (
  <RALayout appBar={CustomAppBar} menu={Menu}>
    {children}
    <CheckForApplicationUpdate />
  </RALayout>
);
