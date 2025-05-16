import React, { useEffect, useState, useCallback } from "react";
import {
  Card,
  CardContent,
  Typography,
  Box,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  TablePagination,
  CircularProgress,
  Chip,
  Button,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
} from "@mui/material";
import { useNavigate } from "react-router-dom";
import { Title } from "react-admin";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import InfoIcon from "@mui/icons-material/Info";
import SearchIcon from "@mui/icons-material/Search";
import { vehicleOperations } from "../../providers/vehicleOperations";

interface Registration {
  id: number;
  type: string;
  registrationInfo: string;
  convertible: boolean;
  conversionData?: string;
  hasConversionHistory: boolean;
}

interface PaginatedResponse {
  content: Registration[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const VehicleRegistrationList = () => {
  const navigate = useNavigate();
  const [data, setData] = useState<PaginatedResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [searchTerm, setSearchTerm] = useState("");
  const [vehicleType, setVehicleType] = useState<string>("");
  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState("");

  useEffect(() => {
    const timerId = setTimeout(() => {
      setDebouncedSearchTerm(searchTerm);
    }, 500);

    return () => {
      clearTimeout(timerId);
    };
  }, [searchTerm]);

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const response = await vehicleOperations.getRegistrationsPaginated(
        page,
        rowsPerPage,
        "id",
        "DESC",
        debouncedSearchTerm,
        vehicleType || undefined,
      );
      setData(response);
    } catch (error) {
      console.error("Error loading registration data:", error);
    } finally {
      setLoading(false);
    }
  }, [page, rowsPerPage, debouncedSearchTerm, vehicleType]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleChangePage = (_event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (
    event: React.ChangeEvent<HTMLInputElement>,
  ) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const handleViewDetails = (id: number) => {
    navigate(`/vehicles/${id}/registration`);
  };

  return (
    <Card>
      <Title title="Vehicle Registrations" />
      <CardContent>
        <Box
          sx={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            mb: 3,
          }}
        >
          <Typography variant="h5">Registration Information</Typography>
          <Button
            variant="outlined"
            startIcon={<ArrowBackIcon />}
            onClick={() => navigate("/vehicles")}
          >
            Back to Vehicles
          </Button>
        </Box>

        <Box sx={{ mb: 3 }}>
          <div style={{ display: 'flex', flexDirection: 'row', gap: '16px', flexWrap: 'wrap' }}>
            <div style={{ flex: 1, minWidth: '250px' }}>
              <TextField
                label="Search VIN or License Plate"
                variant="outlined"
                fullWidth
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                InputProps={{
                  startAdornment: (
                    <SearchIcon sx={{ color: "action.active", mr: 1 }} />
                  ),
                }}
              />
            </div>
            <div style={{ flex: 1, minWidth: '250px' }}>
              <FormControl fullWidth variant="outlined" sx={{ minWidth: 200 }}>
                <InputLabel id="vehicle-type-label">Vehicle Type</InputLabel>
                <Select
                  labelId="vehicle-type-label"
                  id="vehicle-type-select"
                  value={vehicleType}
                  onChange={(e) => setVehicleType(e.target.value as string)}
                  label="Vehicle Type"
                  sx={{ width: '100%' }}
                >
                  <MenuItem value="">All Types</MenuItem>
                  <MenuItem value="DIESEL">Diesel</MenuItem>
                  <MenuItem value="ELECTRIC">Electric</MenuItem>
                  <MenuItem value="GASOLINE">Gasoline</MenuItem>
                </Select>
              </FormControl>
            </div>
          </div>
        </Box>

        {loading ? (
          <Box sx={{ display: "flex", justifyContent: "center", mt: 4, mb: 4 }}>
            <CircularProgress />
          </Box>
        ) : (
          <>
            <TableContainer component={Paper}>
              <Table
                sx={{ minWidth: 650 }}
                aria-label="vehicle registrations table"
              >
                <TableHead>
                  <TableRow>
                    <TableCell>ID</TableCell>
                    <TableCell>Type</TableCell>
                    <TableCell>Registration Info</TableCell>
                    <TableCell>Convertible</TableCell>
                    <TableCell>Conversion Status</TableCell>
                    <TableCell>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {data?.content.map((row) => (
                    <TableRow
                      key={row.id}
                      sx={{ "&:last-child td, &:last-child th": { border: 0 } }}
                    >
                      <TableCell component="th" scope="row">
                        {row.id}
                      </TableCell>
                      <TableCell>{row.type}</TableCell>
                      <TableCell>{row.registrationInfo}</TableCell>
                      <TableCell>
                        <Chip
                          label={row.convertible ? "Yes" : "No"}
                          color={row.convertible ? "success" : "default"}
                          size="small"
                        />
                      </TableCell>
                      <TableCell>
                        {row.hasConversionHistory && (
                          <Chip
                            label="Converted"
                            color="info"
                            size="small"
                          />
                        )}
                      </TableCell>
                      <TableCell>
                        <Button
                          size="small"
                          startIcon={<InfoIcon />}
                          onClick={() => handleViewDetails(row.id)}
                        >
                          Details
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
            <TablePagination
              rowsPerPageOptions={[5, 10, 25]}
              component="div"
              count={data?.totalElements || 0}
              rowsPerPage={rowsPerPage}
              page={page}
              onPageChange={handleChangePage}
              onRowsPerPageChange={handleChangeRowsPerPage}
            />
          </>
        )}
      </CardContent>
    </Card>
  );
};
