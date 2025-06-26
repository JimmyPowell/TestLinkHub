package tech.cspioneer.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.cspioneer.backend.entity.Meeting;
import tech.cspioneer.backend.entity.MeetingParticipant;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.entity.dto.request.MeetingPartReviewRequest;
import tech.cspioneer.backend.entity.dto.request.MeetingParticipantRequest;
import tech.cspioneer.backend.mapper.MeetingMapper;
import tech.cspioneer.backend.mapper.MeetingParticipantMapper;
import tech.cspioneer.backend.mapper.UserMapper;
import tech.cspioneer.backend.service.MeetingPartService;

import java.util.List;


@Service
@RequiredArgsConstructor
public class MeetingPartServicelmpl implements MeetingPartService {
    private MeetingMapper meetingMapper;
    private UserMapper userMapper;
    private MeetingParticipantMapper meetingParticipantMapper;
    private MeetingParticipantMapper meetingPartMapper;


    //审核参会+
    @Override
    public void reviewpart(MeetingPartReviewRequest reviewresult) {
        //1.确定该申请是否处于对应的待审清状态
        MeetingParticipant part = meetingPartMapper.findPartByUuid(reviewresult.getPartUuid());
        if(part == null) {
            throw new RuntimeException("参会申请不存在");
        }
        if(!"pending".equals(part.getStatus())) {
            throw new RuntimeException("该申请不是待审核状态");
        }
        //2.更新审核信息到表里
        meetingPartMapper.updateReviewResult(
                part.getId(),
                reviewresult.getReviewResult(),
                reviewresult.getComments()
        );


    }


    //通过partuuid来查找会议id
    @Override
    public MeetingParticipant findMeetingPartByUuid(String partUuid) {
        return meetingPartMapper.findByUuid(partUuid);
    }

    @Override
    public Boolean isCreator(String useruuid, MeetingParticipant part) {
        // 获取会议信息
        Meeting meeting = meetingMapper.findById(part.getMeetingId());
        if (meeting == null) return false;
        User user = userMapper.findByUuid(useruuid);
        return user.getId().equals(meeting.getCreatorId());
    }

    @Override
    public List<MeetingParticipant> getMeetingPartsByCreator(String useruuid, int page, int size) {
        int offset = (page - 1) * size;
        return meetingPartMapper.findPartsByCreator(useruuid, offset, size);
    }

    @Override
    public List<MeetingParticipant> getMeetingPartsByUser(String useruuid, int page, int size) {
        return List.of();
    }

    @Override
    public void joinMeeting(MeetingParticipantRequest request, String useruuid) {

    }
}
