import { Route } from 'react-router-dom';
import { VehicleConvert, VehicleRegistration } from './resources/vehicles';

export const customRoutes = [
  <Route path="/vehicles/:id/convert" element={<VehicleConvert />} />,
  <Route path="/vehicles/:id/registration" element={<VehicleRegistration />} />,
];
