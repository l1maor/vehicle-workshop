import { List, ListItem, ListItemText, Divider, Box, Typography, Chip } from '@mui/material';
import { Link } from 'react-router-dom';

interface Registration {
  id: number;
  type: string;
  registrationInfo: string;
  convertible: boolean;
  conversionData?: string;
}

interface RegistrationInfoProps {
  registrations: Registration[];
}

export const RegistrationInfo = ({ registrations }: RegistrationInfoProps) => {
  if (!registrations || registrations.length === 0) {
    return (
      <Typography variant="body1">No registration information available.</Typography>
    );
  }

  return (
    <List sx={{ maxHeight: '400px', overflow: 'auto' }}>
      {registrations.map((reg, index) => (
        <Box key={reg.id}>
          {index > 0 && <Divider />}
          <ListItem component={Link} to={`/vehicles/${reg.id}/registration`} sx={{ textDecoration: 'none', color: 'inherit' }}>
            <ListItemText
              primary={
                <Box display="flex" alignItems="center" gap={1}>
                  <Typography variant="subtitle1">{reg.registrationInfo}</Typography>
                  {reg.convertible && <Chip size="small" color="secondary" label="Convertible" />}
                </Box>
              }
              secondary={
                <>
                  <Typography variant="body2" component="span">
                    Type: {reg.type}
                  </Typography>
                  {reg.convertible && reg.conversionData && (
                    <Typography variant="body2" color="text.secondary" component="div">
                      Conversion Data: {reg.conversionData}
                    </Typography>
                  )}
                </>
              }
            />
          </ListItem>
        </Box>
      ))}
    </List>
  );
};
