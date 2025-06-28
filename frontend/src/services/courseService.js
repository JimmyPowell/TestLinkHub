import api from './api';

const courseService = {
  // 获取课程列表
  getLessons(params) {
    return api.get('/admin/lessons', { params });
  },

  // 上传课程
  uploadLesson(lessonData) {
    const requestData = {
      name: lessonData.title,
      description: lessonData.description,
      image_url: lessonData.coverUrl,
      author_name: lessonData.authorName, // 假设有一个作者名字段
      resources_type: 'video', // 假设资源类型为视频
      resources_urls: lessonData.videos.map(video => video.url),
      sort_orders: lessonData.videos.map((_, index) => index + 1)
    };
    return api.post('/admin/lesson/upload', requestData);
  },

  // 更新课程
  updateLesson(uuid, lessonData) {
    const requestData = {
      name: lessonData.title,
      description: lessonData.description,
      image_url: lessonData.coverUrl,
      author_name: lessonData.authorName,
      resources_type: 'video',
      resources_urls: lessonData.videos.map(video => video.url),
      sort_orders: lessonData.videos.map((_, index) => index + 1)
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
