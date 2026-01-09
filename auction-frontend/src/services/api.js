import axios from 'axios';
import { useAuthStore } from '../store/authStore';

const API_BASE_URL = '/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - attach JWT token
api.interceptors.request.use(
  (config) => {
    const token = useAuthStore.getState().token;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor - handle errors globally
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      const { status, data } = error.response;

      if (status === 401) {
        useAuthStore.getState().logout();
        window.location.href = '/login';
      }

      const message = data?.message || 'An unexpected error occurred';
      error.message = message;
      error.errors = data?.errors;
    } else if (error.request) {
      error.message = 'Network error. Please check your connection.';
    }

    return Promise.reject(error);
  }
);

// =====================
// Auth API
// =====================
export const authAPI = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
  getMe: () => api.get('/auth/me'),
  updateProfile: (data) => api.put('/auth/profile', data),
  changePassword: (oldPassword, newPassword) =>
    api.post(`/auth/change-password?oldPassword=${oldPassword}&newPassword=${newPassword}`),
};

// =====================
// Auction API
// =====================
export const auctionAPI = {
  getActive: (page = 0, size = 12, sortBy = 'createdAt', sortDir = 'desc') =>
    api.get(`/auctions?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}`),

  getById: (id) => api.get(`/auctions/${id}`),

  search: (keyword, page = 0, size = 12) =>
    api.get(`/auctions/search?keyword=${keyword}&page=${page}&size=${size}`),

  getByCategory: (category, page = 0, size = 12) =>
    api.get(`/auctions/category/${category}?page=${page}&size=${size}`),

  getFeatured: () => api.get('/auctions/featured'),

  getEndingSoon: (page = 0, size = 12) =>
    api.get(`/auctions/ending-soon?page=${page}&size=${size}`),

  getPopular: (page = 0, size = 12) =>
    api.get(`/auctions/popular?page=${page}&size=${size}`),

  getMyAuctions: (page = 0, size = 12) =>
    api.get(`/auctions/my-auctions?page=${page}&size=${size}`),

  getMyBids: (page = 0, size = 12) =>
    api.get(`/auctions/my-bids?page=${page}&size=${size}`),

  getWon: (page = 0, size = 12) =>
    api.get(`/auctions/won?page=${page}&size=${size}`),

  create: (data) => api.post('/auctions', data),

  update: (id, data) => api.put(`/auctions/${id}`, data),

  cancel: (id) => api.delete(`/auctions/${id}`),
};

// =====================
// Bid API
// =====================
export const bidAPI = {
  placeBid: (data) => api.post('/bids', data),
  getAuctionBids: (auctionId, page = 0, size = 20) =>
    api.get(`/bids/auction/${auctionId}?page=${page}&size=${size}`),
  getMyBids: (page = 0, size = 20) =>
    api.get(`/bids/my-bids?page=${page}&size=${size}`),
};

// =====================
// Notification API
// =====================
export const notificationAPI = {
  getAll: (page = 0, size = 20) =>
    api.get(`/notifications?page=${page}&size=${size}`),
  getUnread: (page = 0, size = 20) =>
    api.get(`/notifications/unread?page=${page}&size=${size}`),
  getUnreadCount: () => api.get('/notifications/unread-count'),
  markAsRead: (id) => api.put(`/notifications/${id}/read`),
  markAllAsRead: () => api.put('/notifications/read-all'),
};

export default api;
