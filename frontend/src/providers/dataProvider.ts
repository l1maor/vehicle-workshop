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
      // Handle both array responses and objects that might contain arrays
      const data = Array.isArray(json) ? json : json.data || json;
      const dataArray = Array.isArray(data) ? data : [data].filter(Boolean);
      
      return {
        data: dataArray,
        total: dataArray.length,
      };
    });
  },

  getOne: (resource, params) =>
    httpClient(`${apiUrl}/${resource}/${params.id}`).then(({ json }) => ({
      data: json,
    })),

  getMany: (resource, params) => {
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

  update: (resource, params) =>
    httpClient(`${apiUrl}/${resource}/${params.id}`, {
      method: 'PUT',
      body: JSON.stringify(params.data),
    }).then(() => ({ data: params.data })),

  updateMany: (resource, params) => {
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

  delete: (resource, params) =>
    httpClient(`${apiUrl}/${resource}/${params.id}`, {
      method: 'DELETE',
    }).then(({ json }) => ({ data: json })),

  deleteMany: (resource, params) =>
    Promise.all(
      params.ids.map(id =>
        httpClient(`${apiUrl}/${resource}/${id}`, {
          method: 'DELETE',
        })
      )
    ).then(responses => ({ data: responses.map(({ json }) => json.id) })),
};
