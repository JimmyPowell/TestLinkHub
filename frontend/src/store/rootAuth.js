import { defineStore } from 'pinia';
import authService from '../services/authService';
import userService from '../services/userService';
import router from '../router';
import { jwtDecode } from 'jwt-decode';

const maskToken = (token) => {
  if (!token) return 'undefined';
  if (token.length <= 15) return '***token too short***';
  return token.substring(0, 10) + '...' + token.substring(token.length - 5);
};

export const useRootAuthStore = defineStore('rootAuth', {
  state: () => ({
    accessToken: localStorage.getItem('rootAccessToken') || null,
    refreshToken: localStorage.getItem('rootRefreshToken') || null,
    userName: localStorage.getItem('rootUserName') || null,
    userRole: localStorage.getItem('rootUserRole') || null,
  }),
  getters: {
    isAuthenticated: (state) => !!state.accessToken,
  },
  actions: {
    async login(credentials) {
      console.info('🔐 开始Root登录流程...');
      try {
        const response = await authService.loginRoot(credentials);
        console.log('✅ Root登录成功，获取Token');
        const { access_token, refresh_token } = response.data.data;
        this.setTokens(access_token, refresh_token);

        await this.fetchCurrentUserInfo();

        console.info('➡️ 导航到Root后台');
        router.push('/root/home');
      } catch (error) {
        console.error('❌ Root登录失败:', error);
        throw error;
      }
    },
    logout() {
      console.info('🚪 开始Root登出流程');
      this.$reset();
      localStorage.removeItem('rootAccessToken');
      localStorage.removeItem('rootRefreshToken');
      localStorage.removeItem('rootUserName');
      localStorage.removeItem('rootUserRole');
      
      console.info('➡️ 导航到Root登录页面');
      router.push('/root/login');
    },
    setTokens(access_token, refresh_token) {
      this.accessToken = access_token;
      this.refreshToken = refresh_token;
      localStorage.setItem('rootAccessToken', access_token);
      localStorage.setItem('rootRefreshToken', refresh_token);
      console.debug('🔑 已保存Root Token: 访问Token:', maskToken(access_token), '刷新Token:', maskToken(refresh_token));
      this.decodeToken(access_token);
    },
    decodeToken(token) {
      if (token) {
        try {
          console.debug('🔍 解析Root Token内容');
          const decoded = jwtDecode(token);
          this.userRole = decoded.identity;
          localStorage.setItem('rootUserRole', decoded.identity);
        } catch (error) {
          console.error('❌ Root Token解析失败:', error);
          this.logout();
        }
      }
    },
    async fetchCurrentUserInfo() {
      try {
        const response = await userService.getMe();
        const { name, role } = response.data.data;
        this.userName = name;
        this.userRole = role;
        localStorage.setItem('rootUserName', name);
        localStorage.setItem('rootUserRole', role);
        console.log(`✅ 欢迎, Root用户 ${this.userName}!`);
      } catch (error) {
        console.error('❌ 获取Root用户信息失败:', error);
      }
    },
    async refreshAccessToken() {
      console.info('🔄 开始刷新Root Token...');
      try {
        const response = await authService.refreshToken(this.refreshToken);
        const { access_token, refresh_token } = response.data.data;
        this.setTokens(access_token, refresh_token);
        return access_token;
      } catch (error) {
        console.error('❌ Root Token刷新失败:', error);
        this.logout();
        return Promise.reject(error);
      }
    },
  },
});
