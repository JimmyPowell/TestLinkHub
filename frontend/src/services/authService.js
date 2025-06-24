import apiClient from './api';

export default {
  generateVerifyCode(email) {
    return apiClient.post('/auth/generate-verify-code', { email });
  },
  verifyCode(email, code) {
    return apiClient.post('/auth/verify-code', { email, code });
  },
  registerEnterprise(data) {
    return apiClient.post('/auth/register/enterprise', data);
  },
  loginCompany(credentials) {
    return apiClient.post('/auth/login/company', credentials);
  },
};
