package tech.cspioneer.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.cspioneer.backend.entity.Company;
import tech.cspioneer.backend.entity.Meeting;
import tech.cspioneer.backend.entity.MeetingVersion;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.entity.dto.request.MeetingCreateRequest;
import tech.cspioneer.backend.entity.dto.request.MeetingReviewRequest;
import tech.cspioneer.backend.entity.dto.request.MeetingUpdateRequest;
import tech.cspioneer.backend.exception.ResourceNotFoundException;
import tech.cspioneer.backend.mapper.*;
import tech.cspioneer.backend.service.MeetingService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class MeetingServicelmpl implements MeetingService {

    @Autowired
    private CompanyMapper companyMapper;
    @Autowired
    private MeetingMapper meetingMapper;
    @Autowired
    private MeetingVersionMapper meetingVersionMapper;
    @Autowired
    private AuditHistoryMapper auditHistoryMapper;
    //偷过来用一下
    private Company getCompanyByUuid(String uuid) {
        Company company = companyMapper.findByUuid(uuid);
        if(company == null ) {
            throw new ResourceNotFoundException("Company", "uuid", uuid);
        }
        return company;
    }
    //新创建会议
    @Override
    public void createMeetingWithVersion(MeetingCreateRequest res, String useruuid ){
        System.out.println("开始创建会议");
        Company company = getCompanyByUuid(useruuid);
        if(company == null) {
            System.out.println("用户不存在");
        }
        LocalDateTime now = LocalDateTime.now();

        //1.创建会议
        Meeting meeting = Meeting.builder()
                .uuid(UUID.randomUUID().toString())
                .creatorId(company.getId())
                .isDeleted(0)
                .status("draft")  //新创建的会议默认为draft
                .createdAt(now)
                .updatedAt(now)
                .build();
        meetingMapper.insert(meeting);
        System.out.println("会议主表创建完成"+"对应会议的uuid为："+meeting.getUuid());

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
                .editorId(company.getId())
                .createdAt(now)
                .build();
        System.out.println("会议版本表创建完成"+"对应会议版本的uuid为："+meetingVersion.toString());
        meetingVersionMapper.insert(meetingVersion);
        System.out.println("会议版本表创建完成");


        //3.更新会议主表
        // private Long currentVersionId;
        // private Long pendingVersionId;
        meeting.setPendingVersionId(meetingVersion.getId());
        meetingMapper.update(meeting);

    }

    @Override
    public void updateMeetingWithVersion(MeetingUpdateRequest res, String useruuid) {
        Company company = getCompanyByUuid(useruuid);
        if(company == null) {
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
                .editorId(company.getId())
                .createdAt(now)
                .build();

        meetingVersionMapper.insert(meetingVersion);

        //3.更新主表
        meeting.setPendingVersionId(meetingVersion.getId());
        meeting.setUpdatedAt(now);
        meetingMapper.update(meeting);

    }

    @Override
    public void deleteMeeting(String meetingUuid) {
        // 1. 查找会议是否存在
        Meeting meeting = meetingMapper.findByUuid(meetingUuid);
        if (meeting == null) {
            throw new ResourceNotFoundException("Meeting", "uuid", meetingUuid);
        }
        // 2. 设置软删除标志
        meeting.setIsDeleted(1);

        // 3. 更新数据库
        meetingMapper.update(meeting);

        System.out.println("删除会议 " + meetingUuid);


    }

    @Override
    public void reviewMeetingCreate(MeetingReviewRequest req, String useruuid) {
        MeetingVersion version = meetingVersionMapper.findByUuid(req.getMeetingVersionUuid());
        if (version == null) {
            throw new ResourceNotFoundException("MeetingVersion", "uuid", req.getMeetingVersionUuid());
        }

        // 写入审核历史记录
        auditHistoryMapper.insertHistory(
                version.getId(),
                Long.parseLong(useruuid),
                req.getAuditStatus(),
                req.getComments(),
                LocalDateTime.now()
        );

         //更新版本状态
        String status = req.getAuditStatus();
        version.setStatus(status);
        meetingVersionMapper.updateStatus(version.getId(), status);

         //如果通过审核，更新会议主表的 current_version_id
        if ("approved".equals(status)) {
            Meeting meeting = meetingMapper.findById(version.getMeetingId());
            meeting.setCurrentVersionId(version.getId());
            meeting.setStatus("active"); // or “approved”
            meetingMapper.update(meeting);
        }
    }

    @Override
    public List<MeetingVersion> getPendingReviewList(int page, int size) {
        int offset = (page - 1) * size;
        return meetingVersionMapper.findPendingList(offset, size);
    }

    @Override
    public MeetingVersion getMeetingVersionDetails(String meetingUuid) {
        System.out.println("meetingUuid<UNK>"+meetingUuid);
        //1.根据meeting_uuid找到会议
        Meeting meeting = meetingMapper.findByUuid(meetingUuid);
        System.out.println("meeting"+meeting.toString());
        if (meeting == null) {
            throw new ResourceNotFoundException("Meeting", "uuid", meetingUuid);
        }
        Long id = meeting.getCurrentVersionId();
        //2.会议对应的版本
        return meetingVersionMapper.findById(id);
    }


    @Override
    public List<MeetingVersion> getPublishedMeetings(int page, int size) {

        // 1. 查询所有发布的会议（meeting 表）
        List<Meeting> publishedMeetings = meetingMapper.findPublishedMeetings();

        // 2. 提取所有 currentVersionId
        List<Long> currentVersionIds = publishedMeetings.stream()
                .map(Meeting::getCurrentVersionId)
                .filter(Objects::nonNull)
                .toList();

        if (currentVersionIds.isEmpty()) {
            return List.of();
        }

        // 3. 查询对应的版本（meeting_version 表）
        List<MeetingVersion> versions = meetingVersionMapper.findVersionsByIds(currentVersionIds);

        // 4. 分页（手动处理，或写 SQL 限制）
        int fromIndex = Math.min((page - 1) * size, versions.size());
        int toIndex = Math.min(fromIndex + size, versions.size());

        return versions.subList(fromIndex, toIndex);
    }
}
