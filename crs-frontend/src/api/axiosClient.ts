import axios from 'axios';

const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,

  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor:
// Tự động đính kèm JWT vào các request cần xác thực
axiosClient.interceptors.request.use((config) => {

  // Login không cần JWT
  const isLoginRequest =
    config.url === '/api/auth/login';

  if (!isLoginRequest) {

    const token =
      localStorage.getItem('crs_token');

    if (token) {
      config.headers.Authorization =
        `Bearer ${token}`;
    }
  }

  return config;
});

// Response Interceptor:
// Xử lý khi token không hợp lệ hoặc hết hạn
axiosClient.interceptors.response.use(

  (response) => response,

  (error) => {

    if (axios.isAxiosError(error)) {

      const status =
        error.response?.status;

      // Chỉ xử lý 401 / 403
      // của các API cần đăng nhập.
      if (
        (status === 401 || status === 403) &&
        !error.config?.url?.includes('/api/auth/login')
      ) {

        localStorage.removeItem('crs_token');
        localStorage.removeItem('crs_user');

        if (
          window.location.pathname !== '/login'
        ) {

          window.location.href = '/login';
        }
      }
    }

    return Promise.reject(error);
  }
);

export default axiosClient;