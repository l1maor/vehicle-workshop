import { fetchUtils } from "react-admin";

const apiUrl = "/api";

type HttpClientOptions = {
  headers?: Headers;
  method?: string;
  body?: string;
};

const httpClient = (url: string, options: HttpClientOptions = {}) => {
  const token = localStorage.getItem("token");
  if (token) {
    options.headers = new Headers({
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    });
  }
  return fetchUtils.fetchJson(url, options);
};

export const vehicleOperations = {
  convertToGas: (vehicleId: string, fuelTypes: string[]) => {
    return httpClient(`${apiUrl}/vehicles/${vehicleId}/convert-to-gas`, {
      method: "POST",
      body: JSON.stringify(fuelTypes),
    }).then(({ json }) => json);
  },

  checkIfConvertible: (vehicleId: string) => {
    return httpClient(`${apiUrl}/vehicles/${vehicleId}/is-convertible`).then(
      ({ json }) => json,
    );
  },

  getConversionHistory: (vehicleId: string) => {
    return httpClient(`${apiUrl}/vehicles/${vehicleId}/conversion-history`).then(
      ({ json }) => json,
    );
  },

  getRegistration: (vehicleId: string) => {
    return httpClient(`${apiUrl}/vehicles/${vehicleId}/registration`).then(
      ({ json }) => json,
    );
  },

  getAllRegistrations: () => {
    return httpClient(`${apiUrl}/vehicles/registration`).then(
      ({ json }) => json,
    );
  },

  getRegistrationsPaginated: (
    page = 0,
    perPage = 10,
    sortField = "id",
    sortOrder = "DESC",
    searchTerm?: string,
    vehicleType?: string,
  ) => {
    const query: Record<string, string | number> = {
      page,
      size: perPage,
      sort: `${sortField},${sortOrder.toLowerCase()}`,
    };

    if (searchTerm) query.searchTerm = searchTerm;
    if (vehicleType) query.type = vehicleType;

    const queryString = Object.keys(query)
      .map((key) => `${key}=${encodeURIComponent(query[key])}`)
      .join("&");

    return httpClient(
      `${apiUrl}/vehicles/registration/search?${queryString}`,
    ).then(({ json }) => json);
  },

  searchVehicles: (
    page = 0,
    perPage = 10,
    sortField = "id",
    sortOrder = "DESC",
    searchTerm?: string,
    vehicleType?: string,
  ) => {
    const query: Record<string, string | number> = {
      page,
      size: perPage,
      sort: `${sortField},${sortOrder.toLowerCase()}`,
    };

    if (searchTerm && searchTerm.trim() !== "")
      query.searchTerm = searchTerm.trim();
    if (vehicleType && vehicleType !== "") query.type = vehicleType;

    const queryString = Object.keys(query)
      .map((key) => `${key}=${encodeURIComponent(query[key])}`)
      .join("&");

    const requestUrl = `${apiUrl}/vehicles/search?${queryString}`;

    return httpClient(requestUrl).then(({ json }) => json);
  },

  getByType: (type: string) => {
    return httpClient(`${apiUrl}/vehicles/type/${type}`).then(
      ({ json }) => json,
    );
  },

  streamEvents: () => {
    const eventSource = new EventSource(`${apiUrl}/vehicles/stream`);
    return eventSource;
  },
};
