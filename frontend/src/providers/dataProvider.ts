import { fetchUtils, DataProvider } from "react-admin";
import queryString from "query-string";

const apiUrl = "/api";

type HttpOptions = {
  headers?: Headers;
  method?: string;
  body?: string;
};

const httpClient = (url: string, options: HttpOptions = {}) => {
  if (!options.headers) {
    options.headers = new Headers({ Accept: "application/json" });
  }
  const token = localStorage.getItem("token");
  if (token) {
    options.headers.set("Authorization", `Bearer ${token}`);
  }
  return fetchUtils.fetchJson(url, options);
};

export const dataProvider: DataProvider = {
  getList: (resource, params) => {
    const { page, perPage } = params.pagination || { page: 1, perPage: 10 };
    const { field, order } = params.sort || { field: "id", order: "ASC" };

    const paginationParams: { page: number; size: number; sort?: string } = {
      page: page - 1,
      size: perPage,
    };

    if (field && field !== "id") {
      paginationParams.sort = `${field},${order.toLowerCase()}`;
    }

    let resourcePath;

    if (
      resource === "vehicles" &&
      params.filter &&
      (params.filter.searchTerm || params.filter.type)
    ) {
      resourcePath = `${resource}/search`;

    } else if (resource === "vehicles/registration") {
      resourcePath = resource;
    } else {
      resourcePath = `${resource}/paginated`;
    }

    let modifiedFilter = { ...params.filter };

    if (modifiedFilter && modifiedFilter.type === 'ALL') {
      delete modifiedFilter.type;
    }

    const query = {
      ...paginationParams,
      ...fetchUtils.flattenObject(modifiedFilter),
    };

    const url = `${apiUrl}/${resourcePath}?${queryString.stringify(query)}`;

    return httpClient(url).then(({ json }) => {
      if (json.content && json.totalElements !== undefined) {
        return {
          data: json.content,
          total: json.totalElements,
        };
      }

      const data = Array.isArray(json) ? json : json.data || json;
      const dataArray = Array.isArray(data) ? data : [data].filter(Boolean);

      return {
        data: dataArray,
        total: dataArray.length,
      };
    });
  },

  getOne: (resource, params) => {
    return httpClient(`${apiUrl}/${resource}/${params.id}`).then(
      ({ json }) => ({
        data: json,
      }),
    );
  },

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
      const data = Array.isArray(json) ? json : json.data || json;
      const dataArray = Array.isArray(data) ? data : [data].filter(Boolean);

      return {
        data: dataArray,
        total: dataArray.length,
      };
    });
  },

  update: (resource, params) => {

    let dataToSend = { ...params.data };


    if (resource === 'vehicles') {
      if (!dataToSend.type && params.previousData && params.previousData.type) {

        dataToSend.type = params.previousData.type;
      }


      if (dataToSend.type === 'GASOLINE' && dataToSend.fuelTypes) {
        if (Array.isArray(dataToSend.fuelTypes)) {

          dataToSend.fuelTypes = dataToSend.fuelTypes.map((item: any) =>
            typeof item === 'object' && item !== null && 'id' in item ? item.id : item
          );
        }
      }
    }



    return httpClient(`${apiUrl}/${resource}/${params.id}`, {
      method: "PUT",
      body: JSON.stringify(dataToSend),
    }).then(({ json }) => ({ data: json || dataToSend }));
  },

  updateMany: (resource, params) => {
    return Promise.all(
      params.ids.map((id) =>
        httpClient(`${apiUrl}/${resource}/${id}`, {
          method: "PUT",
          body: JSON.stringify(params.data),
        }),
      ),
    ).then((responses) => ({
      data: responses
        .map((response) => {
          const { json } = response;
          return json?.id || null;
        })
        .filter((id) => id !== null),
    }));
  },

  create: (resource, params) => {
    let url = `${apiUrl}/${resource}`;
    let dataToSend = { ...params.data };

    if (resource === "vehicles") {
      const vehicleType = params.data.type;
      if (vehicleType === "DIESEL") {
        url = `${apiUrl}/${resource}/diesel`;
      } else if (vehicleType === "ELECTRIC") {
        url = `${apiUrl}/${resource}/electric`;
      } else if (vehicleType === "GASOLINE") {
        url = `${apiUrl}/${resource}/gas`;

        if (dataToSend.fuelTypes && Array.isArray(dataToSend.fuelTypes)) {
          dataToSend.fuelTypes = dataToSend.fuelTypes.map((item: any) => item.id);
        }
      }
    }

    return httpClient(url, {
      method: "POST",
      body: JSON.stringify(dataToSend),
    }).then(({ json }) => {
      if (json && json.id) {
        const result = { ...params.data, ...json };
        return { data: result };
      }

      const resourceWithId = { ...params.data };
      if (!resourceWithId.id) resourceWithId.id = Date.now();
      return { data: resourceWithId };
    }) as Promise<{ data: Record<string, unknown> }>;
  },

  delete: (resource, params) => {
    return httpClient(`${apiUrl}/${resource}/${params.id}`, {
      method: "DELETE",
    }).then(({ json }) => ({ data: json || { id: params.id } }));
  },

  deleteMany: (resource, params) => {
    return Promise.all(
      params.ids.map((id) =>
        httpClient(`${apiUrl}/${resource}/${id}`, {
          method: "DELETE",
        }),
      ),
    ).then((responses) => ({
      data: responses
        .map((response) => {
          const { json } = response;
          return json?.id || null;
        })
        .filter((id) => id !== null),
    }));
  },
};
