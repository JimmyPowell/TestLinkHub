import apiClient from './api';
import { convertToSnakeCase } from '../utils/caseConverter';

const userService = {
  /**
   * 获取用户列表（带筛选和分页）
   * @param {object} params - 查询参数，例如 { page: 0, size: 10, name: 'test' }
   */
  getUsers(params) {
    return apiClient.get('/root/users', { params });
  },

  /**
   * 根据UUID删除用户
   * @param {string} uuid - 用户的UUID
   */
  deleteUser(uuid) {
    return apiClient.delete(`/root/users/${uuid}`);
  },

  /**
   * 创建新用户
   * @param {object} userData - 用户数据 (camelCase)
   */
  createUser(userData) {
    const snakeCaseData = convertToSnakeCase(userData);
    return apiClient.post('/root/users', snakeCaseData);
  },

  /**
   * 更新用户信息
   * @param {string} uuid - 用户的UUID
   * @param {object} userData - 要更新的用户数据 (camelCase)
   */
  updateUser(uuid, userData) {
    const snakeCaseData = convertToSnakeCase(userData);
    return apiClient.put(`/root/users/${uuid}`, snakeCaseData);
  },
};

export default userService;
