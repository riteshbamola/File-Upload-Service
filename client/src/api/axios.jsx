import axios from "axios";
import { setAuthToken } from "./authToken";
import { getAuthToken } from "./authToken";
const api = axios.create({
  baseURL: "http://localhost:5000/",
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use((config) => {
  // ⛔ don’t attach token to refresh endpoint
  if (config.url.includes("/auth/refresh")) {
    return config;
  }
  const token = getAuthToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (res) => res,
  async (error) => {
    const originalRequest = error.config;

    // 🚨 STOP infinite loop
    if (originalRequest.url.includes("/auth/refresh")) {
      return Promise.reject(error);
    }

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const res = await api.post("/auth/refresh");

        const newAccessToken = res.data.accessToken;

        setAuthToken(newAccessToken);

        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

        return api(originalRequest);
      } catch (err) {
        setAuthToken(null);
        return Promise.reject(err);
      }
    }

    return Promise.reject(error);
  },
);

export default api;
