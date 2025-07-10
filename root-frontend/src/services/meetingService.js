import api from './api';

const getReviewMeetings = (params) => {
  return api.get('/root/meeting/reviewlist', { params });
};

const reviewMeeting = (data) => {
  return api.post('/root/meeting/review', data);
};

const deleteMeetings = (meetingUuid) => {
  return api.delete('/root/meeting/delete', { data: { meeting_uuid: meetingUuid } });
};

const getMeetingDetails = (meetingVersionUuid) => {
  return api.get(`/root/meeting/${meetingVersionUuid}`);
};

export default {
  getReviewMeetings,
  reviewMeeting,
  deleteMeetings,
  getMeetingDetails,
};
