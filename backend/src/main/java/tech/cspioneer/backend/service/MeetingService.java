package tech.cspioneer.backend.service;

import tech.cspioneer.backend.entity.MeetingVersion;
import tech.cspioneer.backend.entity.dto.request.MeetingCreateRequest;
import tech.cspioneer.backend.entity.dto.request.MeetingReviewRequest;
import tech.cspioneer.backend.entity.dto.request.MeetingUpdateRequest;
import tech.cspioneer.backend.mapper.MeetingMapper;

import java.util.List;

public interface MeetingService {


    //新创建会议
    public void createMeetingWithVersion(MeetingCreateRequest res, String useruuid );


    //更新会议
    public void updateMeetingWithVersion(MeetingUpdateRequest res, String useruuid);

    //删除会议
    public void deleteMeeting(String meetingUuid);

    void reviewMeetingCreate(MeetingReviewRequest res, String useruuid);

    List<MeetingVersion> getPendingReviewList(int page, int size);

    MeetingVersion getMeetingVersionDetails(String meetingversionuuid);

    List<MeetingVersion> getPublishedMeetings(int page, int size);

    MeetingVersion getMeetingDetails(String meetingUuid);
}
