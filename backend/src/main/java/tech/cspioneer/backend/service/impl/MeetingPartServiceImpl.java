package tech.cspioneer.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.cspioneer.backend.entity.Company;
import tech.cspioneer.backend.entity.Meeting;
import tech.cspioneer.backend.entity.MeetingParticipant;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.entity.dto.request.MeetingPartReviewRequest;
import tech.cspioneer.backend.entity.dto.request.MeetingParticipantRequest;
import tech.cspioneer.backend.entity.dto.response.MeetingApplicationResponse;
import tech.cspioneer.backend.entity.dto.response.MeetingPartResponse;
import tech.cspioneer.backend.exception.ResourceNotFoundException;
import tech.cspioneer.backend.mapper.CompanyMapper;
import tech.cspioneer.backend.mapper.MeetingMapper;
import tech.cspioneer.backend.mapper.MeetingParticipantMapper;
import tech.cspioneer.backend.mapper.UserMapper;
import tech.cspioneer.backend.service.MeetingPartService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MeetingPartServiceImpl implements MeetingPartService {
    @Autowired
    private MeetingMapper meetingMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MeetingParticipantMapper meetingParticipantMapper;
    @Autowired
    private CompanyMapper companyMapper;

    //返回体转化
    private MeetingPartResponse convertToMeetingPartResponse(MeetingParticipant participant) {
        // 查 meetingUuid 和 userUuid（假设你有对应的 Mapper 方法）
        Meeting meeting = meetingMapper.findById(participant.getMeetingId());
//        User user = userMapper.findById(participant.getUserId());

        MeetingPartResponse response = new MeetingPartResponse();
        response.setUuid(participant.getUuid());
        response.setMeetingUuid(meeting != null ? meeting.getUuid() : null);
//        response.setUserUuid(user != null ? user.getUuid() : null);
        response.setJoinReason(participant.getJoinReason());
        response.setStatus(participant.getStatus());
        response.setReviewComment(participant.getReviewComment());
        response.setCreatedAt(participant.getCreatedAt());
        response.setUpdatedAt(participant.getUpdatedAt());

        return response;
    }


    //审核参会+
    @Override
    public void reviewpart(MeetingPartReviewRequest reviewresult) {
        //1.确定该申请是否处于对应的待审清状态
        MeetingParticipant part = meetingParticipantMapper.findPartByUuid(reviewresult.getPartUuid());
        if(part == null) {
            throw new RuntimeException("参会申请不存在");
        }
        if(!"pending".equals(part.getStatus())) {
            throw new RuntimeException("该申请不是待审核状态");
        }
        //2.更新审核信息到表里
        meetingParticipantMapper.updateReviewResult(
                part.getId(),
                reviewresult.getReviewResult(),
                reviewresult.getComments()
        );


    }


    //通过part_uuid来查找会议
    @Override
    public MeetingParticipant findMeetingPartByUuid(String partUuid) {
        return meetingParticipantMapper.findByUuid(partUuid);
    }


    //通过part_uuid来查找申请参会的详细信息
    @Override
    public MeetingPartResponse findMeetingPartByUser(String partUuid) {
        // 查询数据库中 MeetingParticipant 对象
        MeetingParticipant participant = meetingParticipantMapper.findByUuid(partUuid);
        if (participant == null) {
            throw new ResourceNotFoundException("MeetingParticipant", "uuid", partUuid);
        }

        // 转换为返回体
        return convertToMeetingPartResponse(participant);
    }


    @Override
    public Boolean isCreator(String useruuid, MeetingParticipant part) {
        // 获取会议信息
        Meeting meeting = meetingMapper.findById(part.getMeetingId());
        if (meeting == null) return false;

        // The user performing the action is a company admin
        Company company = companyMapper.findByUuid(useruuid);
        if (company == null) {
            return false; // Or throw an exception if user should always exist
        }
        return company.getId().equals(meeting.getCreatorId());
    }

    //这是管理员获取参会申请列表的service，在这里根据管理员获取会议参会申请
    @Override
    public List<MeetingApplicationResponse> getMeetingPartsByCreator(String useruuid, String status, int page, int size) {
        int offset = (page - 1) * size;
        Company company = companyMapper.findByUuid(useruuid);
        if (company == null) {
            throw new ResourceNotFoundException("Company", "uuid", useruuid);
        }
        Long creatorId = company.getId();
        return meetingParticipantMapper.findPartsByCreator(creatorId, status, offset, size);
    }


    //用户查找自己参会信息
    @Override
    public List<MeetingPartResponse> getMeetingPartsByUser(String useruuid, int page, int size) {
        if (useruuid == null || useruuid.trim().isEmpty()) {
            throw new IllegalArgumentException("用户 UUID 不能为空");
        }

        if (page < 1) page = 1;
        if (size <= 0) size = 10;

        int offset = (page - 1) * size;

        // 获取用户 ID
        User user = userMapper.findByUuid(useruuid);
        if (user == null) {
            throw new ResourceNotFoundException("User", "uuid", useruuid);
        }

        Long userId = user.getId();

        // 查询数据库中 MeetingParticipant 列表
        List<MeetingParticipant> participantList = meetingParticipantMapper.findPartsByUser(userId, offset, size);

        // 转换为 MeetingPartResponse
        return participantList.stream()
                .map(this::convertToMeetingPartResponse)
                .collect(Collectors.toList());
    }



    @Override
    public void joinMeeting(MeetingParticipantRequest request, String useruuid) {
        if (request == null || request.getMeetingUuid() == null || request.getMeetingUuid().trim().isEmpty()) {
            throw new IllegalArgumentException("会议 UUID 不能为空");
        }
        // 1. 查找会议 ID
        Meeting meeting = meetingMapper.findByUuid(request.getMeetingUuid());
        if (meeting == null) {
            throw new IllegalArgumentException("会议不存在");
        }
        Long meetingId = meeting.getId();

        // 2. 查找用户 ID
        User user = userMapper.findByUuid(useruuid);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        Long userId = user.getId();

        // 3. 插入记录
        MeetingParticipant participant = MeetingParticipant.builder()
                .uuid(UUID.randomUUID().toString())   // 生成唯一uuid
                .meetingId(meetingId)
                .userId(userId)
                .joinReason(request.getJoinReason())
                .status("pending")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        System.out.println("插入参会申请详情"+participant.toString());

        meetingParticipantMapper.insertParticipant(participant);
    }
}
