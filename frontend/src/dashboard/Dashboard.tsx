import { Card, CardContent, CardHeader, Grid } from '@mui/material';
import { useDataProvider, Title } from 'react-admin';
import { useEffect, useState } from 'react';
// Import components directly from their relative paths
import { VehicleCounts } from './VehicleCounts';
import { RegistrationInfo } from './RegistrationInfo';
import { vehicleOperations } from '../providers/vehicleOperations';

interface VehicleRegistration {
  id: number;
  type: string;
  registrationInfo: string;
  convertible: boolean;
  conversionData?: string;
}

export const Dashboard = () => {
  const dataProvider = useDataProvider();
  const [vehicleStats, setVehicleStats] = useState({
    total: 0,
    diesel: 0,
    electric: 0,
    gasoline: 0,
    convertible: 0
  });
  const [loading, setLoading] = useState(true);
  const [registrations, setRegistrations] = useState<VehicleRegistration[]>([]);

  useEffect(() => {
    Promise.all([
      dataProvider.getList('vehicles', {
        pagination: { page: 1, perPage: 1000 },
        sort: { field: 'id', order: 'ASC' },
        filter: {}
      }),
      vehicleOperations.getAllRegistrations()
    ])
      .then(([vehiclesResponse, registrationsData]) => {
        const vehicles = vehiclesResponse.data;
        const dieselCount = vehicles.filter(v => v.type === 'DIESEL').length;
        const electricCount = vehicles.filter(v => v.type === 'ELECTRIC').length;
        const gasolineCount = vehicles.filter(v => v.type === 'GASOLINE').length;
        const convertibleCount = registrationsData.filter((r: VehicleRegistration) => r.convertible).length;

        setVehicleStats({
          total: vehicles.length,
          diesel: dieselCount,
          electric: electricCount,
          gasoline: gasolineCount,
          convertible: convertibleCount
        });

        setRegistrations(registrationsData);
        setLoading(false);
      })
      .catch(error => {
        console.error('Error loading dashboard data', error);
        setLoading(false);
      });
  }, [dataProvider]);

  if (loading) return <div>Loading...</div>;

  return (
    <div>
      <Title title="Dashboard" />
      
      <Grid container spacing={2}>
        <Grid sx={{ gridColumn: 'span 12', gridColumnMd: 'span 6' }}>
          <Card>
            <CardHeader title="Vehicle Statistics" />
            <CardContent>
              <VehicleCounts stats={vehicleStats} />
            </CardContent>
          </Card>
        </Grid>
        
        <Grid sx={{ gridColumn: 'span 12', gridColumnMd: 'span 6' }}>
          <Card>
            <CardHeader title="Registration Information" />
            <CardContent>
              <RegistrationInfo registrations={registrations} />
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </div>
  );
};
