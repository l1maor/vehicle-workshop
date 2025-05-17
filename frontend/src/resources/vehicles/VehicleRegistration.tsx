import { useEffect, useState } from "react";
import { Title, useRedirect } from "react-admin";
import { useParams } from "react-router-dom";
import { 
  Card, 
  CardContent, 
  Typography, 
  Box, 
  Button, 
  Divider, 
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  IconButton
} from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import { vehicleOperations } from "../../providers/vehicleOperations";

interface ConversionHistory {
  id: number;
  vehicle: {
    id: number;
    vin: string;
    licensePlate: string;
    type: string;
    version: number;
    convertible: boolean;
    fuelTypes: string[];
  };
  conversionDate: string;
  originalBatteryType: string;
  originalVoltage: number;
  originalCurrent: number;
  previousVehicleType: string;
  newVehicleType: string;
  conversionDetails: string | null;
}

export const VehicleRegistration = () => {
  const { id } = useParams();
  const redirect = useRedirect();
  const [registration, setRegistration] = useState<Record<
    string,
    unknown
  > | null>(null);
  const [conversionHistory, setConversionHistory] = useState<ConversionHistory[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedDetails, setSelectedDetails] = useState<{
    details: string;
    fromType: string;
    toType: string;
    date: string;
  } | null>(null);

  useEffect(() => {
    if (!id) return;

    const fetchRegistrationData = async () => {
      try {
        const data = await vehicleOperations.getRegistration(id);
        setRegistration(data);

        if (data.hasConversionHistory) {
          const historyData = await vehicleOperations.getConversionHistory(id);
          if (Array.isArray(historyData)) {
            setConversionHistory(historyData);
          } else if (historyData && historyData.content) {
            setConversionHistory(historyData.content);
          }
        }
      } catch (error) {
        console.error("Error fetching data", error);
      } finally {
        setLoading(false);
      }
    };

    fetchRegistrationData();
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
            <Button variant="contained" onClick={() => redirect("/vehicles")}>
              Back to List
            </Button>
          </Box>
        </CardContent>
      </Card>
    );
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  const typedRegistration = registration as {
    id: number;
    type: string;
    registrationInfo: string;
    convertible: boolean;
    conversionData?: string;
    hasConversionHistory?: boolean;
  };

  return (
    <Card>
      <Title title="Vehicle Registration" />
      <CardContent>
        <Typography variant="h5" gutterBottom>
          Registration Information
        </Typography>
        <Box sx={{ mb: 3 }}>
          <Typography variant="body1">
            <strong>ID:</strong> {typedRegistration.id}
          </Typography>
          <Typography variant="body1">
            <strong>Type:</strong> {typedRegistration.type}
          </Typography>
          <Typography variant="body1">
            <strong>Registration Info:</strong> {typedRegistration.registrationInfo}
          </Typography>
          <Typography variant="body1">
            <strong>Convertible:</strong>{" "}
            {typedRegistration.convertible ? "Yes" : "No"}
          </Typography>

          {typedRegistration.convertible && typedRegistration.conversionData && (
            <Box sx={{ mt: 2 }}>
              <Typography variant="h6">Conversion Data</Typography>
              <Typography variant="body1">
                {typedRegistration.conversionData}
              </Typography>
            </Box>
          )}


          {typedRegistration.hasConversionHistory && conversionHistory.length > 0 && (
            <Box sx={{ mt: 3 }}>
              <Typography variant="h6" gutterBottom>
                <Chip color="info" label="Converted" size="small" sx={{ mr: 1 }} />
                Conversion History
              </Typography>
              <Divider sx={{ my: 2 }} />
              
              <TableContainer component={Paper}>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>From Type</TableCell>
                      <TableCell>To Type</TableCell>
                      <TableCell>Date</TableCell>
                      <TableCell>Original Battery</TableCell>
                      <TableCell>Original Voltage</TableCell>
                      <TableCell>Original Current</TableCell>
                      <TableCell>Details</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {conversionHistory.map((history) => (
                      <TableRow key={history.id}>
                        <TableCell>{history.previousVehicleType}</TableCell>
                        <TableCell>{history.newVehicleType}</TableCell>
                        <TableCell>{formatDate(history.conversionDate)}</TableCell>
                        <TableCell>{history.originalBatteryType}</TableCell>
                        <TableCell>{history.originalVoltage} V</TableCell>
                        <TableCell>{history.originalCurrent} A</TableCell>
                        <TableCell>
                          {history.conversionDetails ? (
                            <Button 
                              size="small" 
                              variant="outlined" 
                              onClick={() => {
                                setSelectedDetails({
                                  details: history.conversionDetails || "",
                                  fromType: history.previousVehicleType,
                                  toType: history.newVehicleType,
                                  date: formatDate(history.conversionDate)
                                });
                                setDialogOpen(true);
                              }}
                            >
                              View
                            </Button>
                          ) : "None"}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
              
              {conversionHistory.length > 0 && conversionHistory[0].conversionDetails && (
                <Box sx={{ mt: 3, p: 2, bgcolor: "background.paper", borderRadius: 1, border: 1, borderColor: "divider" }}>
                  <Typography variant="h6" gutterBottom>Latest Conversion Details</Typography>
                  <Typography variant="body2" style={{ whiteSpace: "pre-wrap" }}>
                    {conversionHistory[0].conversionDetails}
                  </Typography>
                </Box>
              )}
            </Box>
          )}
        </Box>

        <Button variant="contained" onClick={() => redirect("/vehicles")}>
          Back to List
        </Button>
      </CardContent>

      <Dialog 
        open={dialogOpen}
        onClose={() => setDialogOpen(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>
          Conversion Details
          <IconButton
            aria-label="close"
            onClick={() => setDialogOpen(false)}
            sx={{ position: 'absolute', right: 8, top: 8 }}
          >
            <CloseIcon />
          </IconButton>
        </DialogTitle>
        <DialogContent dividers>
          {selectedDetails && (
            <Box>
              <Typography variant="subtitle1" gutterBottom>
                <strong>From:</strong> {selectedDetails.fromType} <strong>To:</strong> {selectedDetails.toType}
              </Typography>
              <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                Converted on {selectedDetails.date}
              </Typography>
              <Divider sx={{ my: 2 }} />
              <Typography variant="body1" style={{ whiteSpace: "pre-wrap" }}>
                {selectedDetails.details}
              </Typography>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>
    </Card>
  );
};
