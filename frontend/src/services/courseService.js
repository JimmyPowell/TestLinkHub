import api from './api';
import { convertToSnakeCase } from '../utils/caseConverter';

const courseService = {
  // 获取所有课程列表（公共）
  getAllLessons(params) {
    return api.get('/user/lesson/all', { params });
  },

  // 获取公司课程列表
  getLessons(params) {
    return api.get('/admin/lesson/company', { params });
  },

  // 上传课程
  uploadLesson(lessonData) {
    const snakeCaseData = convertToSnakeCase(lessonData);
    return api.post('/admin/lesson/upload', snakeCaseData);
  },

  // 更新课程
  updateLesson(uuid, lessonData) {
    const snakeCaseData = convertToSnakeCase(lessonData);
    return api.put(`/admin/lesson/update?uuid=${uuid}`, snakeCaseData);
  },

  // 删除课程
  deleteLesson(uuids) {
    return api.delete('/admin/lesson/delete', { data: uuids });
  },

  // 获取课程详情
  getLessonDetail(uuid) {
    return api.get(`/user/lesson/detail?uuid=${uuid}`);
  }
};

export default courseService;
