import { Route } from 'react-router-dom';
import { VehicleConvert, VehicleRegistration } from './resources/vehicles';

export const customRoutes = [
  <Route key="vehicles-convert" path="/vehicles/:id/convert" element={<VehicleConvert />} />,
  <Route key="vehicles-registration" path="/vehicles/:id/registration" element={<VehicleRegistration />} />,
];
