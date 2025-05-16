import { Route } from "react-router-dom";
import { VehicleConvert, VehicleRegistration } from "./resources/vehicles";
import { VehicleRegistrationList } from "./resources/vehicles/VehicleRegistrationList";

export const customRoutes = [
  <Route
    key="vehicles-convert"
    path="/vehicles/:id/convert"
    element={<VehicleConvert />}
  />,
  <Route
    key="vehicles-registration"
    path="/vehicles/:id/registration"
    element={<VehicleRegistration />}
  />,
  <Route
    key="vehicles-registrations"
    path="/vehicles/registrations"
    element={<VehicleRegistrationList />}
  />,
];
