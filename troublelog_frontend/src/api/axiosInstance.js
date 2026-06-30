
import axios from 'axios'

// 서버 BASE URL - 환경변수로 관리
const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 10_000,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
})

axiosInstance.interceptors.request.use(
  (config) => {
    const fullUrl = typeof window !== "undefined"
      ? new URL(axios.getUri(config), window.location.origin).toString()
      : axios.getUri(config);

    console.log("[request]", {
      method: config.method?.toUpperCase(),
      url: fullUrl,
      params: config.params,
      data: config.data
    });

    return config;
  },
  (error) => {
    console.log("[request error]", error);
    return Promise.reject(error);
  }
);

axiosInstance.interceptors.response.use(
  (response) => {
    console.log("[response]", res.data);

    return res.data;
  },
  (error) => {
    console.log("[response error]", err);
    if (error.response?.status === 401) {
      window.location.href = '/login'
    }
    return Promise.reject(error)
  },
)

export default axiosInstance