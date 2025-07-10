import axios from 'axios';

const apiClient = axios.create({
  baseURL: import.meta.env.PROD ? 'http://testlinkhub.cspioneer.tech:8087/api' : '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

export const setupInterceptors = (store) => {
  apiClient.interceptors.request.use(config => {
    const token = store.accessToken;
    if (token && config.url !== '/auth/refresh') {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  }, error => {
    return Promise.reject(error);
  });

  let isRefreshing = false;
  let failedQueue = [];

  const processQueue = (error, token = null) => {
    failedQueue.forEach(prom => {
      if (error) {
        prom.reject(error);
      } else {
        prom.resolve(token);
      }
    });
    failedQueue = [];
  };

  apiClient.interceptors.response.use(
    response => response,
    async error => {
      const originalRequest = error.config;

      if (error.response.status === 401 && originalRequest.url !== '/auth/refresh') {
        if (isRefreshing) {
          return new Promise((resolve, reject) => {
            failedQueue.push({ resolve, reject });
          }).then(token => {
            originalRequest.headers['Authorization'] = 'Bearer ' + token;
            return apiClient(originalRequest);
          });
        }

        originalRequest._retry = true;
        isRefreshing = true;

        return new Promise((resolve, reject) => {
          store.refreshAccessToken().then(newAccessToken => {
            originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`;
            processQueue(null, newAccessToken);
            resolve(apiClient(originalRequest));
          }).catch(err => {
            processQueue(err, null);
            store.logout();
            reject(err);
          }).finally(() => {
            isRefreshing = false;
          });
        });
      }

      return Promise.reject(error);
    }
  );
};

export default apiClient;
