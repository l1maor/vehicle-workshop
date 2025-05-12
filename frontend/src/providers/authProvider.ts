import { AuthProvider } from 'react-admin';

const authProvider: AuthProvider = {
  login: ({ username, password }) => {
    const request = new Request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
      headers: new Headers({ 'Content-Type': 'application/json' }),
    });
    return fetch(request)
      .then(response => {
        if (response.status < 200 || response.status >= 300) {
          throw new Error(response.statusText);
        }
        return response.json();
      })
      .then(({ token }) => {
        localStorage.setItem('token', token);
        return Promise.resolve();
      });
  },
  
  logout: () => {
    localStorage.removeItem('token');
    return Promise.resolve();
  },
  
  checkAuth: () => {
    const token = localStorage.getItem('token');
    if (!token) {
      return Promise.reject();
    }
    
    // Validate token with backend
    return fetch('/api/auth/validate', {
      method: 'POST',
      headers: new Headers({
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}` 
      }),
      body: JSON.stringify({ token }),
    })
      .then(response => {
        if (response.status < 200 || response.status >= 300) {
          localStorage.removeItem('token'); // Clear invalid token
          throw new Error(response.statusText);
        }
        return response.json();
      })
      .then(json => {
        if (json.valid === false) {
          localStorage.removeItem('token'); // Clear invalid token
          throw new Error('Invalid token');
        }
        return Promise.resolve();
      });
  },
  
  checkError: (error) => {
    const status = error.status;
    if (status === 401) {
      // Only log out for 401 (unauthorized - invalid token)
      localStorage.removeItem('token');
      return Promise.reject();
    }
    if (status === 403) {
      // For 403 (forbidden - insufficient permissions), just stay on the current page
      return Promise.resolve();
    }
    return Promise.resolve();
  },
  
  getPermissions: () => {
    const token = localStorage.getItem('token');
    if (!token) {
      return Promise.reject();
    }
    
    return fetch('/api/users/profile', {
      headers: new Headers({ 
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}` 
      }),
    })
      .then(response => {
        if (response.status < 200 || response.status >= 300) {
          throw new Error(response.statusText);
        }
        return response.json();
      })
      .then(user => user.roles);
  },
};

export default authProvider;
