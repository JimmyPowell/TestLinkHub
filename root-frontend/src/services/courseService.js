import apiClient from './api';
import { convertToSnakeCase } from '../utils/caseConverter';
const courseService = {
  /**
   * 获取待审核的课程列表 (超管)
   * @param {object} params - 分页参数 { page, size }
   */
  getReviewLessons(params) {
    return apiClient.get('/root/lesson/review/list', { params });
  },

  /**
   * 审核课程 (超管)
   * @param {string} uuid - 课程的UUID
   * @param {object} approvalData - 审核数据 { auditStatus, comment }
   */
  approveLesson(uuid, approvalData) {
    const snakeCaseData = convertToSnakeCase(approvalData);
    return apiClient.post(`/root/lesson/review/approval?uuid=${uuid}`, snakeCaseData);
  },

  /**
   * 删除课程 (超管)
   * @param {Array<string>} uuids - 要删除的课程UUID列表
   */
  deleteLessons(uuids) {
    return apiClient.delete('/admin/lesson/delete', { data: uuids });
  },
  
  /**
   * 获取课程详情 (超管预览)
   * @param {string} uuid - 课程的UUID
   */
  getLessonDetail(uuid) {
    return apiClient.get(`/root/lesson/detail/${uuid}`);
  },
};

export default courseService;
