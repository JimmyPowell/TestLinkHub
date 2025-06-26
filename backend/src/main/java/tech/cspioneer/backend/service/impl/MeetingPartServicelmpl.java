package tech.cspioneer.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class MeetingPartServicelmpl implements MeetingPartService {
    @Autowired
    private MeetingMapper meetingMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MeetingParticipantMapper meetingParticipantMapper;
    @Autowired
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


    //通过part_uuid来查找会议
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

    //这是管理员获取参会申请列表的service，在这里根据管理员获取会议参会申请
    @Override
    public List<MeetingParticipant> getMeetingPartsByCreator(String useruuid, int page, int size) {
        int offset = (page - 1) * size;

        return meetingPartMapper.findPartsByCreator(useruuid, offset, size);
    }

    @Override
    public List<MeetingParticipant> getMeetingPartsByUser(String useruuid, int page, int size) {
        if (useruuid == null || useruuid.trim().isEmpty()) {
            throw new IllegalArgumentException("用户 UUID 不能为空");
        }

        if (page < 1) page = 1;
        if (size <= 0) size = 10;

        int offset = (page - 1) * size;

        return meetingPartMapper.findPartsByUser(useruuid, offset, size);
    }

    @Override
    public void joinMeeting(MeetingParticipantRequest request, String useruuid) {
        if (request == null || request.getMeetingUuid() == null || request.getMeetingUuid().trim().isEmpty()) {
            throw new IllegalArgumentException("会议 UUID 不能为空");
        }
        if (useruuid == null || useruuid.trim().isEmpty()) {
            throw new IllegalArgumentException("用户身份无效");
        }

        // 1. 查找会议 ID
        Meeting meeting = meetingMapper.findByUuid(request.getMeetingUuid());
        Long meetingId = meeting.getId();
        if (meetingId == null) {
            throw new IllegalArgumentException("会议不存在");
        }

        // 2. 查找用户 ID
        User user = userMapper.findByUuid(useruuid);
        Long userId = user.getId();
        if (userId == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 3. 插入记录

        MeetingParticipant participant = MeetingParticipant.builder()
                .meetingId(meetingId)
                .userId(userId)
                .joinReason(request.getJoinReason())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        System.out.println("插入参会申请详情"+participant.toString());

        meetingPartMapper.insertParticipant(participant);
    }



}
