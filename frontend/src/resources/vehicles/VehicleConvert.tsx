import { useState, useEffect } from "react";
import {
  useRedirect,
  useNotify,
  Title,
  SelectArrayInput,
  SimpleForm,
  required,
  useGetOne,
} from "react-admin";
import { useParams } from "react-router-dom";
import {
  Card,
  CardContent,
  Typography,
  Box,
  Stepper,
  Step,
  StepLabel,
  Button,
  Paper,
  Divider,
  Alert,
  Grid,
  Chip,
} from "@mui/material";
import { vehicleOperations } from "../../providers/vehicleOperations";
import ElectricCarIcon from "@mui/icons-material/ElectricCar";
import LocalGasStationIcon from "@mui/icons-material/LocalGasStation";
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline";

const steps = [
  "Review Vehicle Details",
  "Configure Gas System",
  "Confirm Conversion",
];

export const VehicleConvert = () => {
  console.log("RENDER :: VehicleConvert");
  const { id } = useParams();
  const redirect = useRedirect();
  const notify = useNotify();
  const [convertible, setConvertible] = useState(false);
  const [loading, setLoading] = useState(true);
  const [activeStep, setActiveStep] = useState(0);
  const [fuelTypes, setFuelTypes] = useState<string[]>([]);
  const { data: vehicle, isLoading } = useGetOne("vehicles", { id });
  console.log({ fuelTypes });
  console.log({ vehicle });
  console.log({ activeStep });
  console.log({ convertible });
  console.log({ loading });
  console.log({ isLoading });
  console.log({ id });

  useEffect(() => {
    if (!id) return;

    vehicleOperations
      .checkIfConvertible(id)
      .then((data) => {
        setConvertible(data);
        setLoading(false);
      })
      .catch(() => {
        notify("Error checking if vehicle is convertible", { type: "error" });
        setLoading(false);
      });
  }, [id, notify]);

  const handleNext = () => {
    setActiveStep((prevStep) => prevStep + 1);
  };

  const handleBack = () => {
    setActiveStep((prevStep) => prevStep - 1);
  };

  const handleFuelTypesChange = (value: string[]) => {
    setFuelTypes(value);
  };

  const handleSubmit = () => {
    vehicleOperations
      .convertToGas(id as string, fuelTypes)
      .then(() => {
        notify("Vehicle converted successfully", { type: "success" });
        setActiveStep(3);
      })
      .catch(() => {
        notify("Error converting vehicle", { type: "error" });
      });
  };

  if (loading || isLoading) {
    return (
      <Card>
        <CardContent>
          <Box display="flex" justifyContent="center" alignItems="center" p={4}>
            <Typography variant="h6">Loading vehicle information...</Typography>
          </Box>
        </CardContent>
      </Card>
    );
  }

  if (!convertible) {
    return (
      <Card>
        <Title title="Convert to Gas" />
        <CardContent>
          <Box display="flex" alignItems="center" mb={2}>
            <ElectricCarIcon color="error" sx={{ fontSize: 40, mr: 2 }} />
            <Typography variant="h5" color="error">
              This vehicle cannot be converted to gas
            </Typography>
          </Box>

          <Alert severity="warning" sx={{ mb: 3 }}>
            Only electric vehicles can be converted to gas. This vehicle is not
            eligible for conversion.
          </Alert>

          <Box mt={3}>
            <Button
              variant="contained"
              onClick={() => redirect("/vehicles")}
              color="primary"
            >
              Back to Vehicles List
            </Button>
          </Box>
        </CardContent>
      </Card>
    );
  }

  const renderStepContent = () => {
    switch (activeStep) {
      case 0:
        return (
          <Paper elevation={2} sx={{ p: 3, mb: 3 }}>
            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <Typography variant="subtitle1" color="text.secondary">
                  Vehicle Details
                </Typography>
                <Divider sx={{ mb: 2 }} />

                <Box sx={{ mb: 2 }}>
                  <Typography variant="body2" color="text.secondary">
                    License Plate
                  </Typography>
                  <Typography variant="h6">{vehicle?.licensePlate}</Typography>
                </Box>

                <Box sx={{ mb: 2 }}>
                  <Typography variant="body2" color="text.secondary">
                    VIN
                  </Typography>
                  <Typography variant="h6">{vehicle?.vin}</Typography>
                </Box>
              </Grid>

              <Grid item xs={12} md={6}>
                <Typography variant="subtitle1" color="text.secondary">
                  Current Configuration
                </Typography>
                <Divider sx={{ mb: 2 }} />

                <Box sx={{ mb: 2, display: "flex", alignItems: "center" }}>
                  <ElectricCarIcon color="primary" sx={{ mr: 1 }} />
                  <Typography variant="h6">Electric Vehicle</Typography>
                </Box>

                <Box sx={{ mb: 2 }}>
                  <Typography variant="body2" color="text.secondary">
                    Battery Type
                  </Typography>
                  <Typography variant="h6">
                    {vehicle?.batteryType || "N/A"}
                  </Typography>
                </Box>

                <Box sx={{ mb: 2 }}>
                  <Typography variant="body2" color="text.secondary">
                    Battery Voltage
                  </Typography>
                  <Typography variant="h6">
                    {vehicle?.batteryVoltage ? Math.round(vehicle.batteryVoltage) : "N/A"}
                  </Typography>
                </Box>

                <Box sx={{ mb: 2 }}>
                  <Typography variant="body2" color="text.secondary">
                    Battery Current
                  </Typography>
                  <Typography variant="h6">
                    {vehicle?.batteryCurrent ? Math.round(vehicle.batteryCurrent) : "N/A"}
                  </Typography>
                </Box>
              </Grid>
            </Grid>
          </Paper>
        );

      case 1:
        return (
          <Paper elevation={2} sx={{ p: 3, mb: 3 }}>
            <Box display="flex" alignItems="center" mb={3}>
              <LocalGasStationIcon
                color="primary"
                sx={{ fontSize: 30, mr: 2 }}
              />
              <Typography variant="h5">Configure Gas System</Typography>
            </Box>

            <Alert severity="info" sx={{ mb: 3 }}>
              The vehicle will be converted to use the following fuel types.
              Please select at least one.
            </Alert>

            <SimpleForm toolbar={false}>
              <SelectArrayInput
                source="fuelTypes"
                choices={[
                  { id: "B83", name: "B83" },
                  { id: "B90", name: "B90" },
                  { id: "B94", name: "B94" },
                  { id: "B100", name: "B100" },
                ]}
                validate={required()}
                onChange={(e) =>
                  handleFuelTypesChange(e.target.value as string[])
                }
              />
            </SimpleForm>

            <Box mt={3}>
              <Typography variant="subtitle1">Selected Fuel Types:</Typography>
              <Box mt={1} display="flex" flexWrap="wrap" gap={1}>
                {fuelTypes.length > 0 ? (
                  fuelTypes.map((type) => (
                    <Chip
                      key={type}
                      label={type}
                      color="primary"
                      icon={<LocalGasStationIcon />}
                    />
                  ))
                ) : (
                  <Typography color="text.secondary">
                    No fuel types selected
                  </Typography>
                )}
              </Box>
            </Box>
          </Paper>
        );

      case 2:
        return (
          <Paper elevation={2} sx={{ p: 3, mb: 3 }}>
            <Box display="flex" alignItems="center" mb={3}>
              <CheckCircleOutlineIcon
                color="warning"
                sx={{ fontSize: 30, mr: 2 }}
              />
              <Typography variant="h5" color="warning.main">
                Confirm Conversion
              </Typography>
            </Box>

            <Alert severity="warning" sx={{ mb: 3 }}>
              <Typography variant="subtitle1" fontWeight="bold">
                Warning: This action cannot be undone
              </Typography>
              <Typography>
                You are about to permanently convert this electric vehicle to
                gas. The original battery information will be stored in
                conversion history, but the vehicle will function as a gas
                vehicle going forward.
              </Typography>
            </Alert>

            <Box
              sx={{
                bgcolor: "background.default",
                p: 2,
                borderRadius: 1,
                mb: 3,
              }}
            >
              <Typography variant="h6">Conversion Summary:</Typography>
              <Divider sx={{ my: 1 }} />

              <Grid container spacing={2}>
                <Grid item xs={6}>
                  <Typography variant="body2" color="text.secondary">
                    Vehicle
                  </Typography>
                  <Typography variant="body1">
                    {vehicle?.licensePlate} (VIN: {vehicle?.vin})
                  </Typography>
                </Grid>

                <Grid item xs={6}>
                  <Typography variant="body2" color="text.secondary">
                    From
                  </Typography>
                  <Typography variant="body1">
                    Electric ({vehicle?.batteryType})
                  </Typography>
                </Grid>

                <Grid item xs={6}>
                  <Typography variant="body2" color="text.secondary">
                    To
                  </Typography>
                  <Typography variant="body1">Gasoline</Typography>
                </Grid>

                <Grid item xs={6}>
                  <Typography variant="body2" color="text.secondary">
                    Compatible Fuels
                  </Typography>
                  <Box display="flex" flexWrap="wrap" gap={0.5} mt={0.5}>
                    {fuelTypes.map?.((type) => (
                      <Chip
                        key={type}
                        label={type}
                        size="small"
                        color="primary"
                      />
                    ))}
                  </Box>
                </Grid>
              </Grid>
            </Box>
          </Paper>
        );

      case 3:
        return (
          <Paper elevation={2} sx={{ p: 3, mb: 3, bgcolor: "success.light" }}>
            <Box display="flex" alignItems="center" mb={3}>
              <CheckCircleOutlineIcon
                color="success"
                sx={{ fontSize: 40, mr: 2 }}
              />
              <Typography variant="h5" color="success.dark">
                Conversion Successful!
              </Typography>
            </Box>

            <Typography variant="body1" paragraph>
              The vehicle has been successfully converted from electric to gas.
            </Typography>

            <Box mt={3}>
              <Button
                variant="contained"
                color="primary"
                onClick={() => redirect("/vehicles")}
              >
                Return to Vehicles List
              </Button>
            </Box>
          </Paper>
        );

      default:
        return null;
    }
  };

  return (
    <Card>
      <Title title="Convert Electric Vehicle to Gas" />
      <CardContent>
        <Stepper activeStep={activeStep} alternativeLabel sx={{ mb: 4 }}>
          {steps.map((label) => (
            <Step key={label}>
              <StepLabel>{label}</StepLabel>
            </Step>
          ))}
        </Stepper>

        {renderStepContent()}

        {activeStep !== 3 && (
          <Box display="flex" justifyContent="space-between" mt={3}>
            <Button
              disabled={activeStep === 0}
              onClick={handleBack}
              variant="outlined"
            >
              Back
            </Button>

            <Box>
              <Button
                variant="outlined"
                onClick={() => redirect("/vehicles")}
                sx={{ mr: 1 }}
              >
                Cancel
              </Button>

              {activeStep === steps.length - 1 ? (
                <Button
                  variant="contained"
                  color="primary"
                  onClick={handleSubmit}
                  disabled={fuelTypes.length === 0}
                >
                  Complete Conversion
                </Button>
              ) : (
                <Button
                  variant="contained"
                  onClick={handleNext}
                  disabled={activeStep === 1 && fuelTypes.length === 0}
                >
                  Next
                </Button>
              )}
            </Box>
          </Box>
        )}
      </CardContent>
    </Card>
  );
};
