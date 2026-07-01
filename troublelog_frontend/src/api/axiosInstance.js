
import axios from 'axios'
import { AUTH_PAGE_PATHS } from '../constants/routePaths.js'

// 서버 BASE URL - 환경변수로 관리
const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 10_000,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
})

let _onLoadingChange = null
let _activeRequests = 0

export const registerLoadingCallback = (cb) => {
  _onLoadingChange = cb       // 외부에서 넘긴 함수를 여기 저장
}

const incrementLoading = () => {
  _activeRequests++
  _onLoadingChange?.(true)    // 저장된 함수를 여기서 실행
}

const decrementLoading = () => {
  _activeRequests = Math.max(0, _activeRequests - 1)
  if (_activeRequests === 0) _onLoadingChange?.(false)
}

axiosInstance.interceptors.request.use(
  (config) => {
    incrementLoading()

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
    decrementLoading()
    
    console.log("[request error]", error);
    return Promise.reject(error);
  }
);

axiosInstance.interceptors.response.use(
  (response) => {
    decrementLoading()

    console.log("[response]", response.data);

    return response.data;
  },
  (error) => {
    decrementLoading()

    console.log("[response error]", error);
    
    const skipAuthRedirect = error.config?.skipAuthRedirect
    const isOnAuthPage = typeof window !== 'undefined' && AUTH_PAGE_PATHS.includes(window.location.pathname)

    if (error.response?.status === 401 && !skipAuthRedirect && !isOnAuthPage) {
      window.location.href = '/login'
    }
    return Promise.reject(error)
  },
)

export default axiosInstance