import { useState, useEffect } from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Typography,
  Chip,
  Box,
  IconButton,
  TablePagination,
  Link as MuiLink,
} from "@mui/material";
import HistoryIcon from "@mui/icons-material/History";
import { Link } from "react-router-dom";
import { vehicleOperations } from "../providers/vehicleOperations";

interface Registration {
  id: number;
  type: string;
  registrationInfo: string;
  convertible: boolean;
  conversionData?: string;
  hasConversionHistory?: boolean;
}

interface RegistrationInfoProps {
  registrations?: Registration[];
}

export const RegistrationInfo = ({
  registrations: initialRegistrations,
}: RegistrationInfoProps) => {
  const [registrations, setRegistrations] = useState<Registration[]>([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalItems, setTotalItems] = useState(0);

  const fetchRegistrations = async (pageNum: number, size: number) => {
    try {
      setLoading(true);
      const response = await vehicleOperations.getRegistrationsPaginated(
        pageNum,
        size,
      );
      setRegistrations(response.content || []);
      setTotalItems(response.totalElements || 0);
      setLoading(false);
    } catch (error) {
      console.error("Error fetching registrations:", error);
      setLoading(false);
    }
  };

  useEffect(() => {
    if (initialRegistrations && initialRegistrations.length > 0) {
      setRegistrations(initialRegistrations.slice(0, rowsPerPage));
      setTotalItems(initialRegistrations.length);
    } else {
      fetchRegistrations(page, rowsPerPage);
    }
  }, [initialRegistrations]);

  const handleChangePage = (_event: unknown, newPage: number) => {
    setPage(newPage);

    if (initialRegistrations && initialRegistrations.length > 0) {
      const start = newPage * rowsPerPage;
      setRegistrations(initialRegistrations.slice(start, start + rowsPerPage));
    } else {
      fetchRegistrations(newPage, rowsPerPage);
    }
  };

  const handleChangeRowsPerPage = (
    event: React.ChangeEvent<HTMLInputElement>,
  ) => {
    const newRowsPerPage = parseInt(event.target.value, 10);
    setRowsPerPage(newRowsPerPage);
    setPage(0);

    if (initialRegistrations && initialRegistrations.length > 0) {
      setRegistrations(initialRegistrations.slice(0, newRowsPerPage));
    } else {
      fetchRegistrations(0, newRowsPerPage);
    }
  };

  if (loading) {
    return (
      <Typography variant="body1">
        Loading registration information...
      </Typography>
    );
  }

  if (!registrations || registrations.length === 0) {
    return (
      <Typography variant="body1">
        No registration information available.
      </Typography>
    );
  }

  return (
    <Box>
      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Registration Info</TableCell>
              <TableCell>Type</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {registrations.map((reg) => (
              <TableRow key={reg.id} hover>
                <TableCell>
                  <MuiLink
                    component={Link}
                    to={`/vehicles/${reg.id}/registration`}
                    color="primary"
                  >
                    {reg.registrationInfo}
                  </MuiLink>
                </TableCell>
                <TableCell>{reg.type}</TableCell>
                <TableCell>
                  <Box display="flex" alignItems="center" gap={1}>
                    {reg.convertible && (
                      <Chip
                        size="small"
                        color="secondary"
                        label="Convertible"
                      />
                    )}
                    {reg.hasConversionHistory && (
                      <Chip size="small" color="info" label="Converted" />
                    )}
                  </Box>
                </TableCell>
                <TableCell>
                  {reg.hasConversionHistory && (
                    <IconButton
                      size="small"
                      component={Link}
                      to={`/vehicles/${reg.id}/registration`}
                      title="View conversion history"
                    >
                      <HistoryIcon fontSize="small" />
                    </IconButton>
                  )}
                  {reg.convertible && reg.conversionData && (
                    <Typography
                      variant="caption"
                      display="block"
                      color="text.secondary"
                    >
                      {reg.conversionData}
                    </Typography>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
      <TablePagination
        component="div"
        count={totalItems}
        page={page}
        onPageChange={handleChangePage}
        rowsPerPage={rowsPerPage}
        onRowsPerPageChange={handleChangeRowsPerPage}
        rowsPerPageOptions={[5, 10, 25]}
      />
    </Box>
  );
};
