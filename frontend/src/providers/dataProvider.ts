import { fetchUtils, DataProvider } from 'react-admin';
import queryString from 'query-string';

const apiUrl = '/api';

// Custom HTTP client that includes auth token in all requests
const httpClient = (url: string, options: any = {}) => {
  if (!options.headers) {
    options.headers = new Headers({ Accept: 'application/json' });
  }
  const token = localStorage.getItem('token');
  if (token) {
    options.headers.set('Authorization', `Bearer ${token}`);
  }
  return fetchUtils.fetchJson(url, options);
};

export const dataProvider: DataProvider = {
  getList: (resource, params) => {
    // Pagination and sorting could be used for server-side implementation if needed
    const query = {
      ...fetchUtils.flattenObject(params.filter),
    };
    const url = `${apiUrl}/${resource}?${queryString.stringify(query)}`;

    return httpClient(url).then(({ json }) => {
      // Special handling for the roles resource which has a different format
      if (resource === 'roles' && json.roles) {
        // The roles data now includes both id and name
        return {
          data: json.roles,
          total: json.roles.length,
        };
      }
      
      // Default handling for other resources
      const data = Array.isArray(json) ? json : json.data || json;
      const dataArray = Array.isArray(data) ? data : [data].filter(Boolean);
      
      return {
        data: dataArray,
        total: dataArray.length,
      };
    });
  },

  getOne: (resource, params) => {
    // For roles, we still need to fetch from the list endpoint since there's no GET by ID endpoint
    if (resource === 'roles') {
      return httpClient(`${apiUrl}/roles`).then(({ json }) => {
        // Find the role with matching id from the roles list
        const paramId = params.id;
        const role = json.roles.find((r: any) => r.id === parseInt(String(paramId), 10) || String(r.id) === String(paramId));
        return { data: role || { id: params.id, name: 'Unknown Role' } };
      });
    }
    
    // Default handling for other resources
    return httpClient(`${apiUrl}/${resource}/${params.id}`).then(({ json }) => ({
      data: json,
    }));
  },

  getMany: (resource, params) => {
    // Special handling for roles resource since individual role endpoints don't exist
    if (resource === 'roles') {
      return httpClient(`${apiUrl}/roles`).then(({ json }) => {
        // Filter roles that match the requested ids
        const matchingRoles = json.roles.filter((r: any) => 
          params.ids.includes(r.id) || params.ids.includes(String(r.id))
        );
        return { data: matchingRoles };
      });
    }
    
    // Default for other resources
    const query = {
      filter: JSON.stringify({ id: params.ids }),
    };
    const url = `${apiUrl}/${resource}?${queryString.stringify(query)}`;
    return httpClient(url).then(({ json }) => ({ data: json }));
  },

  getManyReference: (resource, params) => {
    const query = {
      ...fetchUtils.flattenObject(params.filter),
      [params.target]: params.id,
    };
    const url = `${apiUrl}/${resource}?${queryString.stringify(query)}`;

    return httpClient(url).then(({ json }) => {
      // Handle both array responses and objects that might contain arrays
      const data = Array.isArray(json) ? json : json.data || json;
      const dataArray = Array.isArray(data) ? data : [data].filter(Boolean);
      
      return {
        data: dataArray,
        total: dataArray.length,
      };
    });
  },

  update: (resource, params) => {
    // For roles and all other resources, make a proper PUT request
    return httpClient(`${apiUrl}/${resource}/${params.id}`, {
      method: 'PUT',
      body: JSON.stringify(params.data),
    }).then(({ json }) => ({ data: json || params.data }));
  },

  updateMany: (resource, params) => {
    // For all resources including roles, make proper PUT requests
    return Promise.all(
      params.ids.map(id =>
        httpClient(`${apiUrl}/${resource}/${id}`, {
          method: 'PUT',
          body: JSON.stringify(params.data),
        })
      )
    ).then(responses => ({ data: responses.map(response => {
      const { json } = response;
      return json?.id || null;
    }).filter(id => id !== null) }));
  },

  create: (resource, params) => {
    let url = `${apiUrl}/${resource}`;
    
    if (resource === 'vehicles') {
      const vehicleType = params.data.type;
      if (vehicleType === 'DIESEL') {
        url = `${apiUrl}/${resource}/diesel`;
      } else if (vehicleType === 'ELECTRIC') {
        url = `${apiUrl}/${resource}/electric`;
      } else if (vehicleType === 'GASOLINE') {
        url = `${apiUrl}/${resource}/gas`;
      }
    }

    return httpClient(url, {
      method: 'POST',
      body: JSON.stringify(params.data),
    }).then(({ json }) => {
      const resourceWithId = { ...params.data };
      if (json && json.id) resourceWithId.id = json.id;
      return { data: resourceWithId };
    });
  },

  delete: (resource, params) => {
    // For all resources including roles, use the DELETE endpoint
    return httpClient(`${apiUrl}/${resource}/${params.id}`, {
      method: 'DELETE',
    }).then(({ json }) => ({ data: json || { id: params.id } }));
  },

  deleteMany: (resource, params) => {
    // For all resources including roles, use the DELETE endpoints
    return Promise.all(
      params.ids.map(id =>
        httpClient(`${apiUrl}/${resource}/${id}`, {
          method: 'DELETE',
        })
      )
    ).then(responses => ({ data: responses.map(response => {
      const { json } = response;
      return json?.id || null;
    }).filter(id => id !== null) }));
  },
};
