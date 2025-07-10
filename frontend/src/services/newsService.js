import api from './api';

// 获取所有新闻（管理员/公司）
export const getAllNews = (params) => {
  return api.get('/admin/news/all', { params });
};

// 根据条件查询新闻列表
export const getNewsList = (params) => {
  return api.post('/admin/news/list', params);
};

// 上传/创建新闻
export const uploadNews = (data) => {
  return api.post('/admin/news/upload', data);
};

// 更新新闻
export const updateNews = (uuid, data) => {
  return api.put(`/admin/news/update/${uuid}`, data);
};

// 删除新闻
export const deleteNews = (uuid) => {
  return api.put(`/admin/news/delete/${uuid}`);
};

// 获取新闻详情 (管理员)
export const getAdminNewsDetail = (uuid) => {
  return api.get(`/admin/news/detail/${uuid}`);
};

// 获取待审核新闻列表
export const getNewsForAudit = (params) => {
  return api.get('/root/news/audit', { params });
};

// 获取待审核新闻详情
export const getNewsAuditDetail = (uuid) => {
  return api.get(`/root/news/auditDetail/${uuid}`);
};

// 审核新闻
export const auditNews = (uuid, data) => {
  return api.post(`/root/news/auditNews/${uuid}`, data);
};

// 获取新闻审核历史
export const getNewsAuditHistory = (uuid) => {
  return api.get(`/admin/news/auditHistoryList/${uuid}`);
};
