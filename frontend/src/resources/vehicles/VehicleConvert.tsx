import { useState, useEffect } from 'react';
import {
  useRedirect,
  useNotify,
  Title,
  SelectArrayInput,
  SimpleForm,
  SaveButton,
  required,
  useGetOne
} from 'react-admin';
import { useParams } from 'react-router-dom';
import { Card, CardContent, Typography, Box } from '@mui/material';
import { vehicleOperations } from '../../providers/vehicleOperations';

export const VehicleConvert = () => {
  const { id } = useParams();
  const redirect = useRedirect();
  const notify = useNotify();
  const [convertible, setConvertible] = useState(false);
  const [loading, setLoading] = useState(true);
  const { data: vehicle, isLoading } = useGetOne('vehicles', { id });

  useEffect(() => {
    if (!id) return;
    
    vehicleOperations.checkIfConvertible(id)
      .then(data => {
        setConvertible(data);
        setLoading(false);
      })
      .catch(() => {
        notify('Error checking if vehicle is convertible', { type: 'error' });
        setLoading(false);
      });
  }, [id, notify]);

  const handleSubmit = (values: any) => {
    const { fuelTypes } = values;
    vehicleOperations.convertToGas(id as string, fuelTypes)
      .then(() => {
        notify('Vehicle converted successfully', { type: 'success' });
        redirect('/vehicles');
      })
      .catch(() => {
        notify('Error converting vehicle', { type: 'error' });
      });
  };

  if (loading || isLoading) {
    return <div>Loading...</div>;
  }

  if (!convertible) {
    return (
      <Card>
        <Title title="Convert to Gas" />
        <CardContent>
          <Typography variant="h6" gutterBottom>
            This vehicle cannot be converted to gas
          </Typography>
          <Typography variant="body1">
            Only electric vehicles can be converted to gas.
          </Typography>
          <Box mt={2}>
            <SaveButton
              label="Back to List"
              onClick={() => redirect('/vehicles')}
              variant="outlined"
            />
          </Box>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <Title title="Convert Electric Vehicle to Gas" />
      <CardContent>
        <Typography variant="h6" gutterBottom>
          Converting Vehicle: {vehicle?.licensePlate}
        </Typography>
        <Typography variant="body1" paragraph>
          VIN: {vehicle?.vin}
        </Typography>
        <Typography variant="body1" paragraph color="warning.main">
          Warning: This action will permanently convert this electric vehicle to gas.
          The original battery information will be stored in conversion history.
        </Typography>

        <SimpleForm onSubmit={handleSubmit}>
          <SelectArrayInput
            source="fuelTypes"
            choices={[
              { id: 'B83', name: 'B83' },
              { id: 'B90', name: 'B90' },
              { id: 'B94', name: 'B94' },
              { id: 'B100', name: 'B100' },
            ]}
            validate={required()}
          />
          <Box display="flex" gap={2}>
            <SaveButton label="Convert Vehicle" />
            <SaveButton
              label="Cancel"
              onClick={() => redirect('/vehicles')}
              variant="outlined"
              type="button"
            />
          </Box>
        </SimpleForm>
      </CardContent>
    </Card>
  );
};
