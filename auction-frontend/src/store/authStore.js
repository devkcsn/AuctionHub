import { create } from 'zustand';
import { authAPI } from '../services/api';
import wsService from '../services/websocket';

export const useAuthStore = create((set, get) => ({
  user: JSON.parse(localStorage.getItem('user') || 'null'),
  token: localStorage.getItem('token') || null,
  isAuthenticated: !!localStorage.getItem('token'),
  loading: false,
  error: null,

  login: async (credentials) => {
    set({ loading: true, error: null });
    try {
      const response = await authAPI.login(credentials);
      const { accessToken, user } = response.data.data;

      localStorage.setItem('token', accessToken);
      localStorage.setItem('user', JSON.stringify(user));

      set({
        user,
        token: accessToken,
        isAuthenticated: true,
        loading: false,
      });

      // Connect WebSocket after login
      wsService.connect(accessToken);

      return response.data;
    } catch (error) {
      set({
        loading: false,
        error: error.message || 'Login failed',
      });
      throw error;
    }
  },

  register: async (userData) => {
    set({ loading: true, error: null });
    try {
      const response = await authAPI.register(userData);
      const { accessToken, user } = response.data.data;

      localStorage.setItem('token', accessToken);
      localStorage.setItem('user', JSON.stringify(user));

      set({
        user,
        token: accessToken,
        isAuthenticated: true,
        loading: false,
      });

      wsService.connect(accessToken);

      return response.data;
    } catch (error) {
      set({
        loading: false,
        error: error.message || 'Registration failed',
      });
      throw error;
    }
  },

  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    wsService.disconnect();
    set({
      user: null,
      token: null,
      isAuthenticated: false,
      error: null,
    });
  },

  updateUser: (userData) => {
    localStorage.setItem('user', JSON.stringify(userData));
    set({ user: userData });
  },

  clearError: () => set({ error: null }),

  // Re-initialize WebSocket if token exists
  initializeWebSocket: () => {
    const token = get().token;
    if (token) {
      wsService.connect(token);
    }
  },
}));
