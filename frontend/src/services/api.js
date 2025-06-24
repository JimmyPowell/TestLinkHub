import axios from 'axios';

const apiClient = axios.create({
  baseURL: '/api', // The proxy will handle this
  headers: {
    'Content-Type': 'application/json',
  },
});

// You can add interceptors here if needed, for example, to handle tokens.
apiClient.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, error => {
  return Promise.reject(error);
});

export default apiClient;
