import React from "react";
import {
  List,
  Datagrid,
  TextField,
  EditButton,
  DeleteButton,
  FunctionField,
  Button,
  useRecordContext,
  FilterButton,
  TopToolbar,
  CreateButton,
  useRedirect,
  Pagination,
  useListContext,
  ListProps,
  TextInput,
  SelectInput,
} from "react-admin";

import { Box } from "@mui/material";

import VisibilityIcon from "@mui/icons-material/Visibility";
import ElectricCarIcon from "@mui/icons-material/ElectricCar";

const vehicleFilters = [
  <TextInput
    source="searchTerm"
    label="Search VIN or License Plate"
    alwaysOn
    key="search"
  />,
  <SelectInput
    source="type"
    label="Vehicle Type"
    emptyText={"All Types"}
    emptyValue={""}
    choices={[
      { id: "DIESEL", name: "Diesel" },
      { id: "ELECTRIC", name: "Electric" },
      { id: "GASOLINE", name: "Gasoline" },
    ]}
    alwaysOn
    key="type"
  />,
];

const EmptyResults = () => {
  const { filterValues } = useListContext();
  const hasFilters =
    filterValues && (filterValues.searchTerm || filterValues.type);

  if (hasFilters) {
    return (
      <Box sx={{ p: 2, textAlign: "center" }}>
        <h3>No vehicles match your search criteria</h3>
        <p>Try changing your search terms or filters</p>
        {filterValues.searchTerm && (
          <p>Search term: {filterValues.searchTerm}</p>
        )}
        {filterValues.type && <p>Vehicle type: {filterValues.type}</p>}
      </Box>
    );
  }

  return (
    <Box sx={{ p: 2, textAlign: "center" }}>
      <h3>No vehicles found</h3>
      <p>Add some vehicles to see them here</p>
      <CreateButton variant="contained" />
    </Box>
  );
};

const ListActions = () => {
  return (
    <TopToolbar>
      <FilterButton />
      <CreateButton />
    </TopToolbar>
  );
};

const ConvertToGasButton = () => {
  const record = useRecordContext();
  const redirect = useRedirect();

  if (!record || record.type !== "ELECTRIC" || !record.convertible) {
    return null;
  }

  const handleClick = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    redirect(`/vehicles/${record.id}/convert`);
  };

  return (
    <Button
      onClick={handleClick}
      label="Convert to Gas"
      startIcon={<ElectricCarIcon />}
    />
  );
};

const RegistrationButton = () => {
  const record = useRecordContext();
  const redirect = useRedirect();
  if (!record) return null;

  const handleClick = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    redirect(`/vehicles/${record.id}/registration`);
  };

  return (
    <Button
      onClick={handleClick}
      label="Registration"
      startIcon={<VisibilityIcon />}
    />
  );
};

export const VehicleList = (props: ListProps) => {
  return (
    <List
      {...props}
      actions={<ListActions />}
      pagination={<Pagination rowsPerPageOptions={[10, 25, 50, 100]} />}
      perPage={10}
      sort={{ field: "id", order: "DESC" }}
      disableSorting={true}
      empty={<EmptyResults />}
      filters={vehicleFilters}
      filterDefaultValues={{ type: "ALL" }}
    >
      <Datagrid bulkActionButtons={false} sortable={false}>
        <TextField source="id" sortable={false} />
        <TextField source="vin" label="VIN" sortable={false} />
        <TextField source="licensePlate" label="License Plate" sortable={false} />
        <TextField source="type" sortable={false} />
        <FunctionField
          label="Specific Details"
          sortable={false}
          render={(record: Record<string, unknown>) => {
            if (record.type === "DIESEL") {
              return `Injection Pump: ${record.injectionPumpType || "N/A"}`;
            } else if (record.type === "ELECTRIC") {
              return `Battery: ${record.batteryType || "N/A"}, Voltage: ${typeof record.batteryVoltage === 'number' ? Math.round(record.batteryVoltage) : "N/A"}, Current: ${typeof record.batteryCurrent === 'number' ? Math.round(record.batteryCurrent) : "N/A"}`;
            } else if (record.type === "GASOLINE") {
              return `Fuel Types: ${record.fuelTypes ? record.fuelTypes.join?.(", ") : "N/A"}`;
            }
            return "N/A";
          }}
        />
        <ConvertToGasButton />
        <RegistrationButton />
        <EditButton />
        <DeleteButton />
      </Datagrid>
    </List>
  );
};
