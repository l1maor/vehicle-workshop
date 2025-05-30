import React from 'react';
import { Alert, AlertTitle, List, ListItem, Box } from '@mui/material';
import { useNotify } from 'react-admin';

interface ErrorDisplayProps {
  error?: any;
}

export const ErrorDisplay: React.FC<ErrorDisplayProps> = ({ error }) => {
  const notify = useNotify();
  
  React.useEffect(() => {
    if (error) {
      if (error.status >= 500) {
        notify('Server error. Please try again later.', { type: 'error' });
      }
    }
  }, [error, notify]);
  
  if (!error || !error.body) {
    return null;
  }
  
  if (error.status === 400 && error.body.errors) {
    const errorData = error.body.errors;
    
    if (typeof errorData === 'object' && !Array.isArray(errorData)) {
      return (
        <Box sx={{ mt: 2, mb: 2 }}>
          <Alert severity="error">
            <AlertTitle>Validation Error</AlertTitle>
            <List dense>
              {Object.entries(errorData).map(([field, message]) => (
                <ListItem key={field}>
                  {field}: {String(message)}
                </ListItem>
              ))}
            </List>
          </Alert>
        </Box>
      );
    }
    
    if (Array.isArray(errorData)) {
      return (
        <Box sx={{ mt: 2, mb: 2 }}>
          <Alert severity="error">
            <AlertTitle>Validation Error</AlertTitle>
            <List dense>
              {errorData.map((message, index) => (
                <ListItem key={index}>{String(message)}</ListItem>
              ))}
            </List>
          </Alert>
        </Box>
      );
    }
  }
  
  return (
    <Box sx={{ mt: 2, mb: 2 }}>
      <Alert severity="error">
        <AlertTitle>Error</AlertTitle>
        {error.body.message || 'An unexpected error occurred'}
      </Alert>
    </Box>
  );
};
