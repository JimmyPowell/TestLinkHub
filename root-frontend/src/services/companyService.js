import apiClient from './api';
import { convertToSnakeCase } from '../utils/caseConverter';

const companyService = {
  /**
   * 获取公司列表
   * @param {object} params - 查询参数 (e.g., { page: 0, size: 10, name: 'test' })
   */
  getCompanies(params) {
    // For GET request query parameters, Spring Boot binds them by name directly.
    // The snake_case strategy applies mainly to @RequestBody/@ResponseBody.
    // We send camelCase params to match the DTO fields.
    return apiClient.get('/root/companies', { params });
  },

  /**
   * 创建新公司
   * @param {object} companyData - 公司数据 (camelCase)
   */
  createCompany(companyData) {
    const snakeCaseData = convertToSnakeCase(companyData);
    return apiClient.post('/root/companies', snakeCaseData);
  },

  /**
   * 更新公司信息
   * @param {string} uuid - 公司的UUID
   * @param {object} companyData - 要更新的公司数据 (camelCase)
   */
  updateCompany(uuid, companyData) {
    const snakeCaseData = convertToSnakeCase(companyData);
    return apiClient.put(`/root/companies/${uuid}`, snakeCaseData);
  },

  /**
   * 删除公司
   * @param {string} uuid - 公司的UUID
   */
  deleteCompany(uuid) {
    return apiClient.delete(`/root/companies/${uuid}`);
  },

  /**
   * 更新公司状态
   * @param {string} uuid - 公司的UUID
   * @param {string} status - 新的状态
   */
  updateCompanyStatus(uuid, status) {
    return apiClient.put(`/root/companies/${uuid}/status`, { status });
  },
};

export default companyService;
