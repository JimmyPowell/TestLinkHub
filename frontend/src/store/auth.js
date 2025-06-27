import { defineStore } from 'pinia';
import authService from '../services/authService';
import userService from '../services/userService';
import router from '../router';
import { jwtDecode } from 'jwt-decode';

// 辅助函数：显示部分隐藏的token
const maskToken = (token) => {
  if (!token) return 'undefined';
  if (token.length <= 15) return '***token too short***';
  return token.substring(0, 10) + '...' + token.substring(token.length - 5);
};

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: localStorage.getItem('accessToken') || null,
    refreshToken: localStorage.getItem('refreshToken') || null,
    user: null, // Will be populated on successful login/verification
    userName: localStorage.getItem('userName') || null,
    userRole: localStorage.getItem('userRole') || null,
    members: [],
    pagination: {
      currentPage: 1,
      totalPages: 1,
      totalItems: 0,
      pageSize: 10,
    },
  }),
  getters: {
    isAuthenticated: (state) => !!state.accessToken,
  },
  actions: {
    async login(credentials) {
      console.info('🔐 开始登录流程...');
      try {
        const response = await authService.loginCompany(credentials);
        console.log('✅ 登录成功，获取Token');
        const { access_token, refresh_token } = response.data.data;
        this.accessToken = access_token;
        this.refreshToken = refresh_token;
        localStorage.setItem('accessToken', access_token);
        localStorage.setItem('refreshToken', refresh_token);
        console.debug('🔑 已保存Token: 访问Token:', maskToken(access_token), '刷新Token:', maskToken(refresh_token));
        
        this.decodeToken(access_token);
        await this.fetchCurrentUserInfo();
        
        console.info('➡️ 导航到仪表盘');
        router.push('/dashboard');
      } catch (error) {
        console.error('❌ 登录失败:', error);
        throw error;
      }
    },
    async refreshAccessToken() {
      console.info('🔄 开始刷新Token...');
      console.debug('🔑 当前刷新Token:', maskToken(this.refreshToken));
      
      try {
        const response = await authService.refreshToken(this.refreshToken);
        const access_token = response.data.data.access_token;
        const refresh_token = response.data.data.refresh_token;
        
        console.log('✅ Token刷新成功');
        console.debug('🔑 新访问Token:', maskToken(access_token));
        console.debug('🔑 新刷新Token:', maskToken(refresh_token));
        
        this.accessToken = access_token;
        this.refreshToken = refresh_token;
        localStorage.setItem('accessToken', access_token);
        localStorage.setItem('refreshToken', refresh_token);
        
        this.decodeToken(access_token);
        return access_token;
      } catch (error) {
        console.error('❌ Token刷新失败:', error);
        return Promise.reject(error);
      }
    },
    logout() {
      console.info('🚪 开始登出流程');
      // Also call the backend logout to invalidate the token if the endpoint exists
      // authService.logout({ refresh_token: this.refreshToken });

      console.debug('🧹 清理存储的Token和用户信息');
      this.accessToken = null;
      this.refreshToken = null;
      this.user = null;
      this.userName = null;
      this.userRole = null;
      this.members = [];
      localStorage.removeItem('accessToken');
      localStorage.removeItem('userName');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('userRole');
      
      console.info('➡️ 导航到登录页面');
      router.push('/');
    },
    decodeToken(token) {
      if (token) {
        try {
          console.debug('🔍 解析Token内容');
          const decoded = jwtDecode(token);
          console.debug('🔑 Token包含身份:', decoded.identity);
          this.userRole = decoded.identity;
          localStorage.setItem('userRole', decoded.identity);
        } catch (error) {
          console.error('❌ Token解析失败:', error);
          this.logout();
        }
      } else {
        console.warn('⚠️ 尝试解析空Token');
      }
    },
    async fetchCurrentUserInfo() {
      try {
        const response = await userService.getMe();
        const { name, role } = response.data.data;
        this.userName = name;
        this.userRole = role;
        localStorage.setItem('userName', name);
        localStorage.setItem('userRole', role);
        console.log(`✅ 欢迎, ${this.userName}!`);
      } catch (error) {
        console.error('❌ 获取用户信息失败:', error);
        // Do not logout here, as the token might still be valid for other operations
      }
    },

    async fetchMembers({ page = 0, size = 10, uuid = null, username = null, status = null, phoneNumber = null }) {
      const searchParams = { page, size, uuid, username, status, phoneNumber };
      console.log('📋 开始获取成员列表，参数:', searchParams);
      
      if (!this.userRole) {
        console.debug('🔍 未找到用户角色，尝试从Token解析');
        this.decodeToken(this.accessToken);
      }

      try {
        let response;
        console.debug('👤 当前用户角色:', this.userRole);
        
        const params = { page, size, uuid, username, status, phoneNumber };

        if (this.userRole === 'ADMIN') {
          console.debug('🔍 以管理员身份获取用户');
          // Note: Admin search not implemented in this task, but adding params for consistency
          response = await userService.getAdminUsers(params);
        } else if (this.userRole === 'COMPANY') {
          console.debug('🔍 以企业身份获取用户');
          response = await userService.getCompanyUsers(params);
        } else {
          console.error('❌ 未知的用户角色:', this.userRole);
          return;
        }
        
        console.log('✅ 成功获取成员列表');

        const { content, page_number, total_pages, total_elements } = response.data.data;
        this.members = content;
        this.pagination.currentPage = page_number + 1;
        this.pagination.totalPages = total_pages;
        this.pagination.totalItems = total_elements;
        
        console.debug('📊 获取到', content.length, '名成员，总计', total_elements, '名，共', total_pages, '页');

      } catch (error) {
        console.error('❌ 获取成员失败:', error);
      }
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
