import api from './api';

const courseService = {
  // 获取课程列表
  getLessons(params) {
    return api.get('/admin/lesson/company', { params });
  },

  // 上传课程
  uploadLesson(lessonData) {
    const videos = lessonData.videos || [];
    const requestData = {
      name: lessonData.name,
      description: lessonData.description,
      image_url: lessonData.imageUrl,
      author_name: lessonData.authorName,
      resources_type: 'video',
      resources_urls: videos.map(video => video.url),
      sort_orders: videos.map((_, index) => index + 1)
    };
    return api.post('/admin/lesson/upload', requestData);
  },

  // 更新课程
  updateLesson(uuid, lessonData) {
    const videos = lessonData.videos || [];
    const requestData = {
      name: lessonData.name,
      description: lessonData.description,
      image_url: lessonData.imageUrl,
      author_name: lessonData.authorName,
      resources_type: 'video',
      resources_urls: videos.map(video => video.url),
      sort_orders: videos.map((_, index) => index + 1)
    };
    return api.put(`/admin/lesson/update?uuid=${uuid}`, requestData);
  },

  // 删除课程
  deleteLesson(uuids) {
    return api.delete('/admin/lesson/delete', { data: uuids });
  },

  // 获取课程详情
  getLessonDetail(uuid) {
    return api.get(`/admin/lesson/detail?uuid=${uuid}`);
  }
};

export default courseService;
