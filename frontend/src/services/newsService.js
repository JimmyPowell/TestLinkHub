import apiClient from './api';

/**
 * 获取新闻列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 返回Promise对象
 */
export const getNewsList = (params = {}) => {
  return apiClient.get('/news/list', { params });
};

/**
 * 获取新闻详情
 * @param {string} uuid - 新闻UUID
 * @returns {Promise} 返回Promise对象
 */
export const getNewsDetail = (uuid) => {
  return apiClient.get(`/news/${uuid}`);
};

/**
 * 创建新闻
 * @param {Object} newsData - 新闻数据
 * @returns {Promise} 返回Promise对象
 */
export const createNews = (newsData) => {
  return apiClient.post('/news/create', newsData);
};

/**
 * 更新新闻
 * @param {string} uuid - 新闻UUID
 * @param {Object} newsData - 新闻数据
 * @returns {Promise} 返回Promise对象
 */
export const updateNews = (uuid, newsData) => {
  return apiClient.put(`/news/${uuid}`, newsData);
};

/**
 * 删除新闻
 * @param {string} uuid - 新闻UUID
 * @returns {Promise} 返回Promise对象
 */
export const deleteNews = (uuid) => {
  return apiClient.delete(`/news/${uuid}`);
};

/**
 * 上传新闻图片
 * @param {FormData} formData - 包含图片的FormData对象
 * @returns {Promise} 返回Promise对象
 */
export const uploadNewsImage = (formData) => {
  return apiClient.post('/upload/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
};

export default {
  getNewsList,
  getNewsDetail,
  createNews,
  updateNews,
  deleteNews,
  uploadNewsImage
}; 