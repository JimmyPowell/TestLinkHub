package tech.cspioneer.backend.service;

import tech.cspioneer.backend.entity.MeetingParticipant;
import tech.cspioneer.backend.entity.dto.request.MeetingPartReviewRequest;
import tech.cspioneer.backend.entity.dto.request.MeetingParticipantRequest;

import java.util.List;

//关于参会申请的操作
public interface MeetingPartService {

    //审核参会
    void reviewpart(MeetingPartReviewRequest reviewresult);

    //通过partuuid来查找会议id
    MeetingParticipant findMeetingPartByUuid(String partUuid);


    //判断参会申请的meeting是不是当前user创建的
    Boolean isCreator(String useruuid, MeetingParticipant part);


    //通过当前人员的id查找其对应的所有申请
    List<MeetingParticipant> getMeetingPartsByCreator(String useruuid, int page, int size);

    List<MeetingParticipant> getMeetingPartsByUser(String useruuid, int page, int size);

    void joinMeeting(MeetingParticipantRequest request, String useruuid);
}
