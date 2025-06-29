import api from './api';

/**
 * 创建新会议
 * @param {object} meetingData - 会议数据
 * @returns {Promise}
 */
export const createMeeting = (meetingData) => {
  return api.post('/admin/meeting/create', meetingData);
};

/**
 * 获取会议列表
 * @param {object} params - 查询参数，例如 { page: 1, size: 10 }
 * @returns {Promise}
 */
export const getMeetings = (params) => {
  return api.get('/admin/meeting/version/application/list', { params });
};

/**
 * 更新会议
 * @param {object} meetingData - 会议更新数据
 * @returns {Promise}
 */
export const updateMeeting = (meetingData) => {
  return api.put('/admin/meeting/update', meetingData);
};

/**
 * 删除会议
 * @param {string} meetingUuid - 会议主表的UUID
 * @returns {Promise}
 */
export const deleteMeeting = (meetingUuid) => {
  return api.delete('/admin/meeting/delete', {
    params: { meeting_uuid: meetingUuid }
  });
};

/**
 * 获取会议版本详情
 * @param {string} versionUuid - 会议版本的UUID
 * @returns {Promise}
 */
export const getMeetingDetail = (versionUuid) => {
  return api.get('/admin/meeting/version/detail', {
    params: { uuid: versionUuid }
  });
};

/**
 * 获取参会申请列表
 * @param {object} params - 查询参数，例如 { page: 1, size: 10 }
 * @returns {Promise}
 */
export const getMeetingApplications = (params) => {
  return api.get('/admin/meeting/list', { params });
};

/**
 * 审核参会申请
 * @param {object} reviewData - 审核数据 { part_uuid: string, review_result: 'approved' | 'rejected', comments: string }
 * @returns {Promise}
 */
export const reviewMeetingApplication = (reviewData) => {
  return api.post('/admin/meeting/partreview', reviewData);
};
