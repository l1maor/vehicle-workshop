import { useEffect, useState } from 'react';
import { Title, useRedirect } from 'react-admin';
import { useParams } from 'react-router-dom';
import { Card, CardContent, Typography, Box, Button } from '@mui/material';
import { vehicleOperations } from '../../providers/vehicleOperations';

export const VehicleRegistration = () => {
  const { id } = useParams();
  const redirect = useRedirect();
  const [registration, setRegistration] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    
    vehicleOperations.getRegistration(id)
      .then(data => {
        setRegistration(data);
        setLoading(false);
      })
      .catch((error) => {
        console.error('Error fetching registration data', error);
        setLoading(false);
      });
  }, [id]);

  if (loading) {
    return <div>Loading...</div>;
  }

  if (!registration) {
    return (
      <Card>
        <Title title="Vehicle Registration" />
        <CardContent>
          <Typography variant="h6" color="error">
            Registration information not available
          </Typography>
          <Box mt={2}>
            <Button
              variant="contained"
              onClick={() => redirect('/vehicles')}
            >
              Back to List
            </Button>
          </Box>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <Title title="Vehicle Registration" />
      <CardContent>
        <Typography variant="h5" gutterBottom>
          Registration Information
        </Typography>
        <Box sx={{ mb: 3 }}>
          <Typography variant="body1">
            <strong>ID:</strong> {registration.id}
          </Typography>
          <Typography variant="body1">
            <strong>Type:</strong> {registration.type}
          </Typography>
          <Typography variant="body1">
            <strong>Registration Info:</strong> {registration.registrationInfo}
          </Typography>
          <Typography variant="body1">
            <strong>Convertible:</strong> {registration.convertible ? 'Yes' : 'No'}
          </Typography>
          
          {registration.convertible && registration.conversionData && (
            <Box sx={{ mt: 2 }}>
              <Typography variant="h6">Conversion Data</Typography>
              <Typography variant="body1">{registration.conversionData}</Typography>
            </Box>
          )}
        </Box>

        <Button 
          variant="contained" 
          onClick={() => redirect('/vehicles')}
        >
          Back to List
        </Button>
      </CardContent>
    </Card>
  );
};
