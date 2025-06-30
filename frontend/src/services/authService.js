import apiClient from './api';

// 辅助函数：显示部分隐藏的token
const maskToken = (token) => {
  if (!token) return 'undefined';
  if (token.length <= 15) return '***token too short***';
  return token.substring(0, 10) + '...' + token.substring(token.length - 5);
};

export default {
  generateVerifyCode(email) {
    console.debug(`📤 发送验证码到邮箱: ${email}`);
    return apiClient.post('/auth/generate-verify-code', { email });
  },
  verifyCode(email, code) {
    console.debug(`🔍 验证邮箱: ${email} 的验证码`);
    return apiClient.post('/auth/verify-code', { email, code });
  },
  registerEnterprise(data) {
    console.debug('📝 企业注册:', data.company_name);
    return apiClient.post('/auth/register/enterprise', data);
  },
  loginCompany(credentials) {
    console.debug(`🔑 企业登录请求: ${credentials.email}`);
    return apiClient.post('/auth/login/company', credentials);
  },
  refreshToken(token) {
    console.debug(`🔄 请求刷新Token: ${maskToken(token)}`);
    
    // 检查token是否存在
    if (!token) {
      console.error('❌ 刷新Token缺失，无法发送请求');
      return Promise.reject(new Error('刷新Token不存在'));
    }
    
    return apiClient.post('/auth/refresh', { refresh_token: token })
      .then(response => {
        console.debug('✅ 服务器返回新Token');
        return response;
      })
      .catch(error => {
        console.error('❌ 刷新Token请求失败:', error.response?.status, error.response?.data?.message || error.message);
        throw error;
      });
  },
  loginRoot(credentials) {
    console.debug(`🔑 Root登录请求: ${credentials.email}`);
    return apiClient.post('/auth/login/user', credentials);
  },
};
