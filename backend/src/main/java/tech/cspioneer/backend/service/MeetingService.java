package tech.cspioneer.backend.service;

import tech.cspioneer.backend.entity.MeetingVersion;
import tech.cspioneer.backend.entity.dto.request.MeetingCreateRequest;
import tech.cspioneer.backend.entity.dto.request.MeetingUpdateRequest;
import tech.cspioneer.backend.mapper.MeetingMapper;

public interface MeetingService {


    //新创建会议
    public void createMeetingWithVersion(MeetingCreateRequest res, String useruuid );


    //更新会议
    public void updateMeetingWithVersion(MeetingUpdateRequest res, String useruuid);
}
