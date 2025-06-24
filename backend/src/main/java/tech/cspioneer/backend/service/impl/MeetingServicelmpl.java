package tech.cspioneer.backend.service.impl;

import tech.cspioneer.backend.entity.Meeting;
import tech.cspioneer.backend.entity.MeetingVersion;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.entity.dto.request.MeetingCreateRequest;
import tech.cspioneer.backend.entity.dto.request.MeetingUpdateRequest;
import tech.cspioneer.backend.exception.ResourceNotFoundException;
import tech.cspioneer.backend.mapper.MeetingMapper;
import tech.cspioneer.backend.mapper.MeetingVersionMapper;
import tech.cspioneer.backend.mapper.UserMapper;
import tech.cspioneer.backend.service.MeetingService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;


public class MeetingServicelmpl implements MeetingService {

    private UserMapper userMapper;
    private MeetingMapper meetingMapper;
    private MeetingVersionMapper meetingVersionMapper;

    //偷过来用一下
    private User getUserByUuid(String uuid) {
        User user = userMapper.findByUuid(uuid);
        if (user == null) {
            throw new ResourceNotFoundException("User", "uuid", uuid);
        }
        return user;
    }
    //新创建会议
    @Override
    public void createMeetingWithVersion(MeetingCreateRequest res, String useruuid ){
        User user = getUserByUuid(useruuid);
        if(user == null) {
            System.out.println("用户不存在");
        }
        LocalDateTime now = LocalDateTime.now();

        //1.创建会议
        Meeting meeting = Meeting.builder()
                .uuid(UUID.randomUUID().toString())
                .creatorId(user.getId())
                .status("draft")  //新创建的会议默认为draft
                .createdAt(now)
                .updatedAt(now)
                .build();
        meetingMapper.insert(meeting);

        //2.同时创建版本号为1的会议版本
        MeetingVersion meetingVersion = MeetingVersion.builder()
                .uuid(UUID.randomUUID().toString())
                .meetingId(meeting.getId())
                .version(1)                     // 第一版=1
                .name(res.getName())
                .description(res.getDescription())
                .coverImageUrl(res.getImageUrl())
                .startTime(LocalDateTime.parse(res.getStartTime()))
                .endTime(LocalDateTime.parse(res.getEndTime()))
                .status("pending_review")       // 版本状态待审核
                .editorId(user.getId())
                .createdAt(now)
                .build();

        meetingVersionMapper.insert(meetingVersion);


        //3.更新会议主表
        // private Long currentVersionId;
        // private Long pendingVersionId;
        meeting.setPendingVersionId(meetingVersion.getId());
        meetingMapper.update(meeting);

    }

    @Override
    public void updateMeetingWithVersion(MeetingUpdateRequest res, String useruuid) {
        User user = getUserByUuid(useruuid);
        if(user == null) {
            System.out.println("用户不存在");
        }
        LocalDateTime now = LocalDateTime.now();

        //1.查询会议主表，确认会议是否存在+查找第几个版本
        Meeting meeting = meetingMapper.findByUuid(res.getMeetingUuid());
        if(meeting == null) {
            throw new ResourceNotFoundException("Meeting", "uuid", res.getMeetingUuid());
        }

        Integer maxVersion = meetingVersionMapper.findMaxVersionByMeetingId(meeting.getId());
        int newVersion = (maxVersion == null ? 1 : maxVersion + 1);

        //2.创建会议版本
        MeetingVersion meetingVersion = MeetingVersion.builder()
                .uuid(UUID.randomUUID().toString())
                .meetingId(meeting.getId())
                .version(newVersion)
                .name(res.getName())
                .description(res.getDescription())
                .coverImageUrl(res.getImageUrl())
                .startTime(LocalDateTime.parse(res.getStartTime()))
                .endTime(LocalDateTime.parse(res.getEndTime()))
                .status("pending_review")
                .editorId(user.getId())
                .createdAt(now)
                .build();

        meetingVersionMapper.insert(meetingVersion);

        //3.更新主表
        meeting.setPendingVersionId(meetingVersion.getId());
        meeting.setUpdatedAt(now);
        meetingMapper.update(meeting);

    }
}
