import { Box, Typography, Grid } from '@mui/material';
import DirectionsCarIcon from '@mui/icons-material/DirectionsCar';
import ElectricCarIcon from '@mui/icons-material/ElectricCar';
import LocalGasStationIcon from '@mui/icons-material/LocalGasStation';
import BuildIcon from '@mui/icons-material/Build';
import InventoryIcon from '@mui/icons-material/Inventory';

interface VehicleStats {
  total: number;
  diesel: number;
  electric: number;
  gasoline: number;
  convertible: number;
}

interface VehicleCountsProps {
  stats: VehicleStats;
}

export const VehicleCounts = ({ stats }: VehicleCountsProps) => {
  return (
    <Grid container spacing={2}>
      <Grid sx={{ gridColumn: 'span 6', gridColumnSm: 'span 4' }}>
        <StatItem 
          icon={<InventoryIcon fontSize="large" color="primary" />}
          label="Total Vehicles"
          value={stats.total}
        />
      </Grid>
      
      <Grid sx={{ gridColumn: 'span 6', gridColumnSm: 'span 4' }}>
        <StatItem 
          icon={<DirectionsCarIcon fontSize="large" color="secondary" />}
          label="Diesel"
          value={stats.diesel}
        />
      </Grid>
      
      <Grid sx={{ gridColumn: 'span 6', gridColumnSm: 'span 4' }}>
        <StatItem 
          icon={<ElectricCarIcon fontSize="large" style={{ color: '#2E7D32' }} />}
          label="Electric"
          value={stats.electric}
        />
      </Grid>
      
      <Grid sx={{ gridColumn: 'span 6', gridColumnSm: 'span 4' }}>
        <StatItem 
          icon={<LocalGasStationIcon fontSize="large" style={{ color: '#ED6C02' }} />}
          label="Gasoline"
          value={stats.gasoline}
        />
      </Grid>
      
      <Grid sx={{ gridColumn: 'span 6', gridColumnSm: 'span 4' }}>
        <StatItem 
          icon={<BuildIcon fontSize="large" style={{ color: '#9C27B0' }} />}
          label="Convertible"
          value={stats.convertible}
        />
      </Grid>
    </Grid>
  );
};

interface StatItemProps {
  icon: React.ReactNode;
  label: string;
  value: number;
}

const StatItem = ({ icon, label, value }: StatItemProps) => {
  return (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        flexDirection: 'column',
        p: 1,
        textAlign: 'center',
      }}
    >
      {icon}
      <Typography variant="h4" component="div">
        {value}
      </Typography>
      <Typography variant="body2" color="text.secondary">
        {label}
      </Typography>
    </Box>
  );
};
