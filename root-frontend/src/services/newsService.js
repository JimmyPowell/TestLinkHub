import apiClient from './api';
import { convertToSnakeCase } from '../utils/caseConverter';

const newsService = {
  /**
   * 获取待审核的新闻列表
   * @param {object} params - 分页参数 { page, pageSize }
   */
  getAuditNews(params) {
    return apiClient.get('/root/news/audit', { params });
  },

  /**
   * 获取待审核新闻的详情
   * @param {string} uuid - 新闻内容的UUID
   */
  getNewsAuditDetail(uuid) {
    return apiClient.get(`/root/news/auditDetail/${uuid}`);
  },

  /**
   * 审核新闻
   * @param {string} uuid - 新闻内容的UUID
   * @param {object} reviewData - 审核数据 { auditStatus, comments }
   */
  auditNews(uuid, reviewData) {
    const snakeCaseData = convertToSnakeCase(reviewData);
    return apiClient.post(`/root/news/auditNews/${uuid}`, snakeCaseData);
  },
};

export default newsService;
