import { Card, CardContent, CardHeader, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Button, Snackbar, Alert } from "@mui/material";
import { useDataProvider, Title, useNotify } from "react-admin";
import { useEffect, useState } from "react";

import { VehicleCounts } from "./VehicleCounts";
import { RegistrationInfo } from "./RegistrationInfo";

export const Dashboard = () => {
  const dataProvider = useDataProvider();
  const notify = useNotify();
  const [vehicleStats, setVehicleStats] = useState({
    total: 0,
    diesel: 0,
    electric: 0,
    gasoline: 0,
    convertible: 0,
  });
  const [loading, setLoading] = useState(true);
  const [openSeedDialog, setOpenSeedDialog] = useState(false);
  const [seeding, setSeeding] = useState(false);
  const [seedSuccess, setSeedSuccess] = useState(false);

  useEffect(() => {
    dataProvider
      .getList("vehicles", {
        pagination: { page: 1, perPage: 1000 },
        sort: { field: "id", order: "ASC" },
        filter: {},
      })
      .then((vehiclesResponse) => {
        const vehicles = vehiclesResponse.data;
        const dieselCount = vehicles.filter((v) => v.type === "DIESEL").length;
        const electricCount = vehicles.filter(
          (v) => v.type === "ELECTRIC",
        ).length;
        const gasolineCount = vehicles.filter(
          (v) => v.type === "GASOLINE",
        ).length;
        const convertibleCount = vehicles.filter(
          (v) => v.convertible === true,
        ).length;

        setVehicleStats({
          total: vehicles.length,
          diesel: dieselCount,
          electric: electricCount,
          gasoline: gasolineCount,
          convertible: convertibleCount,
        });

        setLoading(false);
      })
      .catch((error) => {
        console.error("Error loading dashboard data", error);
        setLoading(false);
      });
  }, [dataProvider]);

  useEffect(() => {
    if (!loading && vehicleStats.total === 0) {
      setOpenSeedDialog(true);
    }
  }, [loading, vehicleStats.total]);

  const handleSeedDatabase = () => {
    setSeeding(true);
    
    fetch('/api/test/seed/large/1000', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
    })
      .then(response => {
        if (!response.ok) {
          throw new Error('Failed to seed database');
        }
        return response.text();
      })
      .then(() => {
        setSeedSuccess(true);
        setSeeding(false);
        setOpenSeedDialog(false);
        
        setLoading(true);
        dataProvider
          .getList("vehicles", {
            pagination: { page: 1, perPage: 1000 },
            sort: { field: "id", order: "ASC" },
            filter: {},
          })
          .then((vehiclesResponse) => {
            const vehicles = vehiclesResponse.data;
            const dieselCount = vehicles.filter((v) => v.type === "DIESEL").length;
            const electricCount = vehicles.filter(
              (v) => v.type === "ELECTRIC",
            ).length;
            const gasolineCount = vehicles.filter(
              (v) => v.type === "GASOLINE",
            ).length;
            const convertibleCount = vehicles.filter(
              (v) => v.convertible === true,
            ).length;

            setVehicleStats({
              total: vehicles.length,
              diesel: dieselCount,
              electric: electricCount,
              gasoline: gasolineCount,
              convertible: convertibleCount,
            });

            setLoading(false);
          })
          .catch((error) => {
            console.error("Error loading dashboard data", error);
            setLoading(false);
          });
      })
      .catch(error => {
        console.error('Error seeding database:', error);
        notify('Error seeding database: ' + error.message, { type: 'error' });
        setSeeding(false);
      });
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
        gap: "16px",
        width: "100%",
      }}
    >
      <Title title="Dashboard" />

      <Card style={{ width: "100%" }}>
        <CardHeader title="Vehicle Statistics" />
        <CardContent>
          <VehicleCounts stats={vehicleStats} />
        </CardContent>
      </Card>

      <Card style={{ width: "100%" }}>
        <CardHeader title="Registration Information" />
        <CardContent>
          <RegistrationInfo />
        </CardContent>
      </Card>


      <Dialog 
        open={openSeedDialog} 
        onClose={() => setOpenSeedDialog(false)}
        aria-labelledby="seed-dialog-title"
        aria-describedby="seed-dialog-description"
      >
        <DialogTitle id="seed-dialog-title">No Vehicles Found</DialogTitle>
        <DialogContent>
          <DialogContentText id="seed-dialog-description">
            There are no vehicles in the database. Would you like to seed the database with 1000 test vehicles?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenSeedDialog(false)} color="primary">
            No, Thanks
          </Button>
          <Button 
            onClick={handleSeedDatabase} 
            color="primary" 
            variant="contained"
            disabled={seeding}
          >
            {seeding ? 'Seeding...' : 'Yes, Seed Database'}
          </Button>
        </DialogActions>
      </Dialog>


      <Snackbar 
        open={seedSuccess} 
        autoHideDuration={5000} 
        onClose={() => setSeedSuccess(false)}
      >
        <Alert onClose={() => setSeedSuccess(false)} severity="success">
          Successfully seeded database with 1000 test vehicles!
        </Alert>
      </Snackbar>
    </div>
  );
};
