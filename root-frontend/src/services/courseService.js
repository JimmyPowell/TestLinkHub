import apiClient from './api';
import { convertToSnakeCase } from '../utils/caseConverter';

const courseService = {
  /**
   * 获取待审核的课程列表
   * @param {object} params - 分页参数 { page, size }
   */
  getReviewCourses(params) {
    return apiClient.get('/root/lesson/review/list', { params });
  },

  /**
   * 审核课程
   * @param {string} uuid - 课程版本的UUID
   * @param {object} reviewData - 审核数据 { auditStatus, comment }
   */
  approveLesson(uuid, reviewData) {
    const snakeCaseData = convertToSnakeCase(reviewData);
    return apiClient.post(`/root/lesson/review/approval?uuid=${uuid}`, snakeCaseData);
  },
};

export default courseService;
