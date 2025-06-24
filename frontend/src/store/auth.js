import { defineStore } from 'pinia';
import authService from '../services/authService';
import router from '../router';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: localStorage.getItem('accessToken') || null,
    refreshToken: localStorage.getItem('refreshToken') || null,
    user: null, // Will be populated on successful login/verification
  }),
  getters: {
    isAuthenticated: (state) => !!state.accessToken,
  },
  actions: {
    async login(credentials) {
      const response = await authService.loginCompany(credentials);
      const { access_token, refresh_token } = response.data.data;
      this.accessToken = access_token;
      this.refreshToken = refresh_token;
      localStorage.setItem('accessToken', access_token);
      localStorage.setItem('refreshToken', refresh_token);
      
      // After login, you might want to fetch user details
      // await this.fetchUser();
      
      router.push('/dashboard');
    },
    logout() {
      this.accessToken = null;
      this.refreshToken = null;
      this.user = null;
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      router.push('/');
    },
    // Action to verify token and fetch user info, needs a backend endpoint
    // async fetchUser() {
    //   try {
    //     const response = await authService.getMe(); // Assumes a getMe endpoint exists
    //     this.user = response.data;
    //   } catch (error) {
    //     console.error("Failed to fetch user, logging out.", error);
    //     this.logout();
    //   }
    // },
  },
});
