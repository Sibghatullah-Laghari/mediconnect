import axios from 'axios';
import { clearTokens, getAccessToken, getRefreshToken, setTokens } from '../auth/tokenManager.js';

export const API_BASE_URL = 'http://localhost:8080/api/v1';
export const RESOLVED_API_BASE_URL = import.meta.env.VITE_API_BASE_URL || API_BASE_URL;

const axiosInstance = axios.create({
  baseURL: RESOLVED_API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

let refreshInFlight = null;
let unauthorizedHandler = () => {};

export function registerUnauthorizedHandler(handler) {
  unauthorizedHandler = handler;
}

async function refreshAccessToken() {
  if (refreshInFlight) {
    return refreshInFlight;
  }

  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    throw new Error('No refresh token available');
  }

  refreshInFlight = axios
    .post(`${RESOLVED_API_BASE_URL}/auth/refresh`, { refreshToken })
    .then(({ data }) => {
      setTokens(data.token, data.refreshToken);
      return data.token;
    })
    .finally(() => {
      refreshInFlight = null;
    });

  return refreshInFlight;
}

axiosInstance.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (!error.response || !originalRequest) {
      return Promise.reject(error);
    }

    const isAuthRequest = originalRequest.url?.includes('/auth/login') || originalRequest.url?.includes('/auth/register');
    if (error.response.status !== 401 || originalRequest._retry || isAuthRequest) {
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    try {
      const nextToken = await refreshAccessToken();
      originalRequest.headers.Authorization = `Bearer ${nextToken}`;
      return axiosInstance(originalRequest);
    } catch (refreshError) {
      clearTokens();
      unauthorizedHandler();
      return Promise.reject(refreshError);
    }
  }
);

export default axiosInstance;
