package tech.cspioneer.backend.service;

import tech.cspioneer.backend.entity.MeetingVersion;
import tech.cspioneer.backend.entity.dto.request.MeetingCreateRequest;
import tech.cspioneer.backend.entity.dto.response.MeetingVersionWithMeetingUuidResponse;
import tech.cspioneer.backend.entity.dto.request.MeetingReviewRequest;
import tech.cspioneer.backend.entity.dto.request.MeetingUpdateRequest;
import tech.cspioneer.backend.entity.dto.response.RootReviewResponse;
import tech.cspioneer.backend.mapper.MeetingMapper;

import java.time.LocalDateTime;
import java.util.List;

public interface MeetingService {


    //新创建会议
    public void createMeetingWithVersion(MeetingCreateRequest res, String useruuid );


    //更新会议
    public void updateMeetingWithVersion(MeetingUpdateRequest res, String useruuid);

    //新创建会议
    public void createMeeting(MeetingCreateRequest res, String useruuid );


    //更新会议
    public void updateMeeting(MeetingUpdateRequest res, String useruuid);

    //删除会议
    public void deleteMeeting(String meetingUuid);

    void reviewMeetingCreate(MeetingReviewRequest res, String useruuid);

    List<RootReviewResponse> getPendingReviewList(int page, int size, String status, String name);

    RootReviewResponse getMeetingVersionDetails(String meetingversionuuid);

    List<RootReviewResponse> getPublishedMeetings(int page, int size);

    RootReviewResponse getMeetingDetails(String meetingUuid);

    List<MeetingVersionWithMeetingUuidResponse> getMeetingVersionsByCreator(
            String creatorUuid,
            String name,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int page,
            int size
    );
    public MeetingVersion getMeetingVersionDetail(String versionUuid, String userUuid);

    List<RootReviewResponse> searchMeetingVersionsByName(String name, int page, int size);

    List<RootReviewResponse> searchMeetingVersionsByStartTimeRange(LocalDateTime start, LocalDateTime end, int page, int size);

    List<RootReviewResponse> searchMeetingVersionsByUuid(String uuid, int page, int size);

}
