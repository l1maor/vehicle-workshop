import { Box, Typography, Grid } from "@mui/material";
import { useNavigate } from "react-router-dom";
import DirectionsCarIcon from "@mui/icons-material/DirectionsCar";
import ElectricCarIcon from "@mui/icons-material/ElectricCar";
import LocalGasStationIcon from "@mui/icons-material/LocalGasStation";
import BuildIcon from "@mui/icons-material/Build";
import InventoryIcon from "@mui/icons-material/Inventory";

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
  const navigate = useNavigate();

  const handleNavigate = (filter?: {
    type?: string;
    convertible?: boolean;
  }) => {
    let path = "/vehicles";

    if (filter) {
      const params = new URLSearchParams();

      if (filter.type) {
        params.append("filter", JSON.stringify({ type: filter.type }));
      } else if (filter.convertible !== undefined) {
        params.append(
          "filter",
          JSON.stringify({ convertible: filter.convertible }),
        );
      }

      if (params.toString()) {
        path += `?${params.toString()}`;
      }
    }

    navigate(path);
  };

  return (
    <Grid container spacing={2}>
      <Grid sx={{ gridColumn: "span 6", gridColumnSm: "span 4" }}>
        <StatItem
          icon={<InventoryIcon fontSize="large" color="primary" />}
          label="Total Vehicles"
          value={stats.total}
          onClick={() => handleNavigate()}
        />
      </Grid>

      <Grid sx={{ gridColumn: "span 6", gridColumnSm: "span 4" }}>
        <StatItem
          icon={<DirectionsCarIcon fontSize="large" color="secondary" />}
          label="Diesel"
          value={stats.diesel}
          onClick={() => handleNavigate({ type: "DIESEL" })}
        />
      </Grid>

      <Grid sx={{ gridColumn: "span 6", gridColumnSm: "span 4" }}>
        <StatItem
          icon={
            <ElectricCarIcon fontSize="large" style={{ color: "#2E7D32" }} />
          }
          label="Electric"
          value={stats.electric}
          onClick={() => handleNavigate({ type: "ELECTRIC" })}
        />
      </Grid>

      <Grid sx={{ gridColumn: "span 6", gridColumnSm: "span 4" }}>
        <StatItem
          icon={
            <LocalGasStationIcon
              fontSize="large"
              style={{ color: "#ED6C02" }}
            />
          }
          label="Gasoline"
          value={stats.gasoline}
          onClick={() => handleNavigate({ type: "GASOLINE" })}
        />
      </Grid>

      <Grid sx={{ gridColumn: "span 6", gridColumnSm: "span 4" }}>
        <StatItem
          icon={<BuildIcon fontSize="large" style={{ color: "#9C27B0" }} />}
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
  onClick?: () => void;
}

const StatItem = ({ icon, label, value, onClick }: StatItemProps) => {
  return (
    <Box
      sx={{
        display: "flex",
        alignItems: "center",
        flexDirection: "column",
        p: 1,
        textAlign: "center",
        cursor: onClick ? "pointer" : "default",
        transition: "transform 0.2s, background-color 0.2s",
        borderRadius: 1,
        "&:hover": onClick
          ? {
              transform: "scale(1.05)",
              backgroundColor: "rgba(0, 0, 0, 0.04)",
            }
          : {},
      }}
      onClick={onClick}
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
