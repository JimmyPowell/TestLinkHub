package tech.cspioneer.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.cspioneer.backend.entity.Company;
import tech.cspioneer.backend.entity.Meeting;
import tech.cspioneer.backend.entity.MeetingVersion;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.entity.dto.request.MeetingCreateRequest;
import tech.cspioneer.backend.entity.dto.request.MeetingReviewRequest;
import tech.cspioneer.backend.entity.dto.request.MeetingUpdateRequest;
import tech.cspioneer.backend.entity.dto.response.MeetingVersionWithMeetingUuidResponse;
import tech.cspioneer.backend.entity.dto.response.RootReviewResponse;
import tech.cspioneer.backend.exception.ResourceNotFoundException;
import tech.cspioneer.backend.mapper.*;
import tech.cspioneer.backend.service.MeetingService;
import tech.cspioneer.backend.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;


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

    @Autowired
    private UserMapper userMapper;
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
    public void createMeeting(MeetingCreateRequest res, String useruuid) {
       //超级管理员创建会议
        LocalDateTime now = LocalDateTime.now();
        User user = userMapper.findByUuid(useruuid);
        if(user == null) {
            System.out.println("找不到超级管理员");
        }
        // 创建会议主表，管理员创建时，状态为published
        String meetingStatus = "published";
        String versionStatus = "active";

        Meeting meeting = Meeting.builder()
                .uuid(UUID.randomUUID().toString())
                .creatorId(user.getId())
                .isDeleted(0)
                .status(meetingStatus)
                .createdAt(now)
                .updatedAt(now)
                .build();
        meetingMapper.insert(meeting);

        // 创建会议版本
        MeetingVersion meetingVersion = MeetingVersion.builder()
                .uuid(UUID.randomUUID().toString())
                .meetingId(meeting.getId())
                .version(1)
                .name(res.getName())
                .description(res.getDescription())
                .coverImageUrl(res.getImageUrl())
                .startTime(LocalDateTime.parse(res.getStartTime()))
                .endTime(LocalDateTime.parse(res.getEndTime()))
                .status(versionStatus)
                .editorId(user.getId())
                .createdAt(now)
                .build();

        meetingVersionMapper.insert(meetingVersion);
        System.out.println("插入后的 meeting version id = " + meetingVersion.getId()); // 👈 这必须有值

        // 更新主表指向最新版本
        meeting.setCurrentVersionId(meetingVersion.getId());
        System.out.println("<UNK> meeting id = " + meeting.getId());
        meetingMapper.update(meeting);
    }

    @Override
    public void updateMeeting(MeetingUpdateRequest res, String useruuid) {
        User user = userMapper.findByUuid(useruuid);
        if(user == null) {
            System.out.println("超级管理员不存在");
        }
        LocalDateTime now = LocalDateTime.now();

        Meeting meeting = meetingMapper.findByUuid(res.getMeetingUuid());
        if (meeting == null) {
            throw new ResourceNotFoundException("Meeting", "uuid", res.getMeetingUuid());
        }

        String versionStatus = "active";
        Integer maxVersion = meetingVersionMapper.findMaxVersionByMeetingId(meeting.getId());
        int newVersion = (maxVersion == null ? 1 : maxVersion + 1);

        MeetingVersion meetingVersion = MeetingVersion.builder()
                .uuid(UUID.randomUUID().toString())
                .meetingId(meeting.getId())
                .version(newVersion)
                .name(res.getName())
                .description(res.getDescription())
                .coverImageUrl(res.getImageUrl())
                .startTime(LocalDateTime.parse(res.getStartTime()))
                .endTime(LocalDateTime.parse(res.getEndTime()))
                .status(versionStatus)
                .editorId(user.getId())
                .createdAt(now)
                .build();

        meetingVersionMapper.insert(meetingVersion);

        meeting.setPendingVersionId(meetingVersion.getId());
        meeting.setUpdatedAt(now);
        //直接更新主表对应的版本
        if ("published".equals(versionStatus)) {
            meeting.setCurrentVersionId(meetingVersion.getId());
        }
        meetingMapper.update(meeting);
    }

    @Override
    @Transactional
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

        // 4. 级联软删除所有关联的会议版本
        meetingVersionMapper.softDeleteByMeetingId(meeting.getId());

        System.out.println("删除会议 " + meetingUuid + " 及其所有版本");
    }

    @Override
    public void reviewMeetingCreate(MeetingReviewRequest req, String useruuid) {
        // 1. 校验版本是否存在
        MeetingVersion version = meetingVersionMapper.findByUuid(req.getMeetingVersionUuid());
        if (version == null) {
            throw new ResourceNotFoundException("MeetingVersion", "uuid", req.getMeetingVersionUuid());
        }

        // 2. 校验审核人是否存在
        User user = userMapper.findByUuid(useruuid);
        if (user == null) {
            throw new ResourceNotFoundException("User", "uuid", useruuid);
        }

        // 3. 获取审核状态，统一小写处理，确保兼容 ENUM 定义
        String auditStatus = req.getAuditStatus();
        if (auditStatus == null) {
            throw new IllegalArgumentException("审核状态不能为空");
        }
        auditStatus = auditStatus.toLowerCase();
        if (!auditStatus.equals("approved") && !auditStatus.equals("rejected")) {
            throw new IllegalArgumentException("Invalid audit status: " + auditStatus);
        }

        // 4. 写入审核历史记录
        auditHistoryMapper.insertHistory(
                version.getId(),
                user.getId(),
                auditStatus,
                req.getComments(),
                LocalDateTime.now()
        );

        // 5. 更新版本状态（与数据库 ENUM 保持一致）
        String versionStatus = auditStatus.equals("approved") ? "active" : "rejected";
        version.setStatus(versionStatus);
        meetingVersionMapper.updateStatus(version.getId(), versionStatus);

        // 6. 如果通过审核，更新主表 current_version_id & 状态
        if (auditStatus.equals("approved")) {
            Meeting meeting = meetingMapper.findById(version.getMeetingId());
            meeting.setCurrentVersionId(version.getId());
            meeting.setStatus("published"); // 对应 meeting 表 ENUM
            meetingMapper.update(meeting);
        }
    }


    private Company getCompanyById(Long editorId) {
        Company company = companyMapper.findById(editorId);
        if (company == null) {
            throw new RuntimeException("未找到对应公司，ID: " + editorId);
        }
        return company;
    }
    //将meeting_version转化为无id的返回体
    private RootReviewResponse convertToRootReview(MeetingVersion version) {
        Meeting meeting = meetingMapper.findById(version.getMeetingId());
        String meeting_uuid= meeting.getUuid();
        Company company = getCompanyById(version.getEditorId());
        String company_uuid= company.getUuid();

        RootReviewResponse resp = new RootReviewResponse();
        resp.setUuid(version.getUuid());
        resp.setMeetingUuid(meeting_uuid);
        resp.setVersion(version.getVersion());
        resp.setName(version.getName());
        resp.setDescription(version.getDescription());
        resp.setCoverImageUrl(version.getCoverImageUrl());
        resp.setStartTime(version.getStartTime());
        resp.setEndTime(version.getEndTime());
        resp.setStatus(version.getStatus());
        resp.setEditorUuid(company_uuid);
        resp.setCreatedAt(version.getCreatedAt());
        return resp;
    }


    //超级管理员获取申请的会议列表
    @Override
    public List<RootReviewResponse> getPendingReviewList(int page, int size) {
        int offset = (page - 1) * size;
        List<MeetingVersion> versions = meetingVersionMapper.findPendingList(offset, size);
        return versions.stream()
                .map(this::convertToRootReview)
                .collect(Collectors.toList());
    }

    //超级管理员获取申请的会议详情
    @Override
    public RootReviewResponse getMeetingVersionDetails(String meetingVersionUuid) {
        MeetingVersion version = meetingVersionMapper.findByUuid(meetingVersionUuid);
        if (version == null) {
            throw new RuntimeException("未找到该版本信息：" + meetingVersionUuid);
        }
        return convertToRootReview(version);
    }

    @Override
    public RootReviewResponse getMeetingDetails(String meetingUuid) {
        // 1. 根据会议主表 UUID 查会议主表对象
        Meeting meeting = meetingMapper.findByUuid(meetingUuid);
        if (meeting == null) {
            throw new ResourceNotFoundException("Meeting", "uuid", meetingUuid);
        }

        // 2. 拿当前生效版本ID
        Long currentVersionId = meeting.getCurrentVersionId();
        if (currentVersionId == null) {
            throw new ResourceNotFoundException("MeetingVersion", "meeting_id", meeting.getId());
        }

        // 3. 根据版本ID查找版本详情
        MeetingVersion version = meetingVersionMapper.findById(currentVersionId);
        if (version == null) {
            throw new ResourceNotFoundException("MeetingVersion", "id", currentVersionId.toString());
        }

        // 4. 封装为 RootReviewResponse 返回
        return convertToRootReview(version);
    }



    @Override
    public List<RootReviewResponse> getPublishedMeetings(int page, int size) {
        // 1. 查询所有已发布的会议
        List<Meeting> publishedMeetings = meetingMapper.findPublishedMeetings();

        // 2. 提取 current_version_id
        List<Long> currentVersionIds = publishedMeetings.stream()
                .map(Meeting::getCurrentVersionId)
                .filter(Objects::nonNull)
                .toList();

        if (currentVersionIds.isEmpty()) {
            return List.of();
        }

        // 3. 查询所有对应版本
        List<MeetingVersion> versions = meetingVersionMapper.findVersionsByIds(currentVersionIds);

        // 4. 分页处理（手动分页）
        int fromIndex = Math.min((page - 1) * size, versions.size());
        int toIndex = Math.min(fromIndex + size, versions.size());
        List<MeetingVersion> pagedVersions = versions.subList(fromIndex, toIndex);

        // 5. 转换为 RootReviewResponse
        return pagedVersions.stream()
                .map(this::convertToRootReview)
                .toList();
    }


    @Override
    public List<MeetingVersionWithMeetingUuidResponse> getMeetingVersionsByCreator(
            String creatorUuid, String name, LocalDateTime startTime, LocalDateTime endTime, int page, int size) {
        Company company = companyMapper.findByUuid(creatorUuid);
        if (company == null) {
            throw new ResourceNotFoundException("Company", "uuid", creatorUuid);
        }
        Long creatorId = company.getId();
        int offset = (page - 1) * size;

        return meetingVersionMapper.findMeetingVersionsByCreator(creatorId, name, startTime, endTime, offset, size);
    }


    @Override
    public MeetingVersion getMeetingVersionDetail(String versionUuid, String userUuid) {
        // 1. 获取当前版本
        MeetingVersion version = meetingVersionMapper.findByUuid(versionUuid);
        if (version == null || version.getIsDeleted() == 1) {
            throw new ResourceNotFoundException("MeetingVersion", "uuid", versionUuid);
        }

        // 2. 获取该会议的主记录
        Meeting meeting = meetingMapper.findById(version.getMeetingId());
        if (meeting == null || meeting.getIsDeleted() == 1) {
            throw new ResourceNotFoundException("Meeting", "id", version.getMeetingId().toString());
        }

        // 3. 获取当前用户并校验权限
        Company user = companyMapper.findByUuid(userUuid);
        if (user == null) {
            // This case should ideally not happen if user is authenticated
            throw new ResourceNotFoundException("User", "uuid", userUuid);
        }
        Long companyId = user.getId();
        if (!meeting.getCreatorId().equals(companyId)) {
            // For security, we can throw a generic not found, or a specific access denied exception.
            // Throwing not found is safer as it doesn't reveal the existence of the resource.
            throw new ResourceNotFoundException("MeetingVersion", "uuid", versionUuid);
        }

        return version;
    }
    @Override
    public List<RootReviewResponse> searchMeetingVersionsByName(String name, int page, int size) {
        int offset = (page - 1) * size;
        List<MeetingVersion> versions = meetingVersionMapper.searchByName(name, offset, size);
        return versions.stream().map(this::convertToRootReview).toList();
    }

    @Override
    public List<RootReviewResponse> searchMeetingVersionsByStartTimeRange(LocalDateTime start,
                                                                          LocalDateTime end,
                                                                          int page, int size) {
        int offset = (page - 1) * size;
        List<MeetingVersion> versions = meetingVersionMapper.searchByStartTimeRange(start, end, offset, size);
        return versions.stream().map(this::convertToRootReview).toList();
    }

    @Override
    public List<RootReviewResponse> searchMeetingVersionsByUuid(String uuid, int page, int size) {
        int offset = (page - 1) * size;
        List<MeetingVersion> versions = meetingVersionMapper.searchByUuid(uuid, offset, size);
        return versions.stream().map(this::convertToRootReview).toList();
    }







}
