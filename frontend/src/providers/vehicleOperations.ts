import { fetchUtils } from 'react-admin';

const apiUrl = '/api';
const httpClient = fetchUtils.fetchJson;

export const vehicleOperations = {
  convertToGas: (vehicleId: string, fuelTypes: string[]) => {
    return httpClient(`${apiUrl}/vehicles/${vehicleId}/convert-to-gas`, {
      method: 'POST',
      body: JSON.stringify(fuelTypes),
    }).then(({ json }) => json);
  },

  checkIfConvertible: (vehicleId: string) => {
    return httpClient(`${apiUrl}/vehicles/${vehicleId}/is-convertible`).then(({ json }) => json);
  },

  getRegistration: (vehicleId: string) => {
    return httpClient(`${apiUrl}/vehicles/${vehicleId}/registration`).then(({ json }) => json);
  },

  getAllRegistrations: () => {
    return httpClient(`${apiUrl}/vehicles/registration`).then(({ json }) => json);
  },

  getByType: (type: string) => {
    return httpClient(`${apiUrl}/vehicles/type/${type}`).then(({ json }) => json);
  },

  streamEvents: () => {
    const eventSource = new EventSource(`${apiUrl}/vehicles/stream`);
    return eventSource;
  }
};
