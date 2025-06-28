package tech.cspioneer.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.cspioneer.backend.entity.*;
import tech.cspioneer.backend.entity.dto.response.*;
import tech.cspioneer.backend.entity.enums.RelatedObjectType;
import tech.cspioneer.backend.mapper.*;
import tech.cspioneer.backend.service.LessonService;
import tech.cspioneer.backend.exception.LessonServiceException;
import tech.cspioneer.backend.entity.dto.request.LessonDetailRequest;
import tech.cspioneer.backend.entity.dto.request.LessonSearchRequest;
import tech.cspioneer.backend.service.NotificationService;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class LessonServiceImpl implements LessonService {
    @Autowired
    private LessonMapper lessonMapper;
    @Autowired
    private LessonVersionMapper lessonVersionMapper;
    @Autowired
    private LessonResourceMapper lessonResourceMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CompanyMapper companyMapper;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private LessonAuditHistoryMapper lessonAuditHistoryMapper;

    @Override
    @Transactional
    public int uploadLesson(Map<String, Object> lessonRequestBody) {
        String identity = (String) lessonRequestBody.get("identity");
        String uuid = (String) lessonRequestBody.get("userId");
        try {
            // 1. 处理资源数据 - 适配新的请求体结构
            List<String> resourcesUrls = null;
            Object resourcesUrlsObj = lessonRequestBody.get("resourcesUrls");
            if (resourcesUrlsObj instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<String> tmp = (List<String>) resourcesUrlsObj;
                resourcesUrls = tmp;
            }
            
            String resourcesType = (String) lessonRequestBody.get("resourcesType");
            // 验证resourcesType是否为有效值
            if (resourcesType != null && !isValidResourceType(resourcesType)) {
                throw new LessonServiceException("无效的资源类型: " + resourcesType + "，允许的类型: video, audio, document, image, link, other");
            }
            
            List<Integer> sortOrders = null;
            Object sortOrdersObj = lessonRequestBody.get("sortOrders");
            if (sortOrdersObj instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<Integer> tmp = (List<Integer>) sortOrdersObj;
                sortOrders = tmp;
            }
            
            // 2. 创建课程版本
            LessonVersion version = new LessonVersion();
            version.setUuid(UUID.randomUUID().toString());
            version.setLessonId(null); // 课程id待创建后补充
            version.setVersion(1); // 首次上传为1
            version.setName((String) lessonRequestBody.get("name"));
            version.setDescription((String) lessonRequestBody.get("description"));
            version.setImageUrl((String) lessonRequestBody.get("imageUrl"));
            version.setAuthorName((String) lessonRequestBody.get("authorName"));
            version.setSortOrder(0);
            version.setIsDeleted(0);
            version.setCreatedAt(LocalDateTime.now());
            
            // 业务分流：管理员 or 企业用户
            Lesson lesson = new Lesson();
            lesson.setUuid(UUID.randomUUID().toString());
            lesson.setIsDeleted(0);
            lesson.setCreatedAt(LocalDateTime.now());
            lesson.setUpdatedAt(LocalDateTime.now());
            
            if ("COMPANY".equals(identity)) {
                Company company = companyMapper.findByUuid(uuid);
                lesson.setPublisherId(company.getId());
                lesson.setStatus("pending_review");
                lesson.setCurrentVersionId(null);
                lesson.setPendingVersionId(0L);
                lessonMapper.insert(lesson);
                Lesson newLesson = lessonMapper.selectByUuid(lesson.getUuid());
                version.setLessonId(newLesson.getId());
                version.setCreatorId(company.getId());
                version.setStatus("pending_review");
                lessonVersionMapper.insert(version);
                lesson.setPendingVersionId(lessonVersionMapper.selectByUuid(version.getUuid()).getId());
                lessonMapper.update(lesson);
            } else if ("ADMIN".equals(identity)) {
                User user = userMapper.findByUuid(uuid);
                lesson.setPublisherId(user.getId());
                lesson.setStatus("active");
                lesson.setCurrentVersionId(0L);
                lesson.setPendingVersionId(null);
                lessonMapper.insert(lesson);
                Lesson newLesson = lessonMapper.selectByUuid(lesson.getUuid());
                version.setLessonId(newLesson.getId());
                version.setCreatorId(user.getId());
                version.setStatus("active");
                lessonVersionMapper.insert(version);
                lesson.setCurrentVersionId(lessonVersionMapper.selectByUuid(version.getUuid()).getId());
                lessonMapper.update(lesson);
                user.setLessonCount(user.getLessonCount() + 1);
                userMapper.update(user);
            } else {
                return -1; // 其他身份不允许
            }
            
            // 3. 处理资源 - 使用新的数据结构
            if (resourcesUrls != null && !resourcesUrls.isEmpty()) {
                for (int i = 0; i < resourcesUrls.size(); i++) {
                    LessonResources resource = new LessonResources();
                    resource.setUuid(UUID.randomUUID().toString());
                    resource.setLessonVersionId(version.getId());
                    resource.setResourcesUrl(resourcesUrls.get(i));
                    resource.setResourcesType(resourcesType != null ? resourcesType : "other");
                    if (sortOrders != null && i < sortOrders.size() && sortOrders.get(i) != null) {
                        resource.setSortOrder(sortOrders.get(i));
                    } else {
                        resource.setSortOrder(0);
                    }
                    resource.setStatus("active");
                    resource.setIsDeleted(0);
                    resource.setCreatedAt(LocalDateTime.now());
                    resource.setUpdatedAt(LocalDateTime.now());
                    lessonResourceMapper.insert(resource);
                }
            }
            return 1;
        } catch (Exception e) {
            throw new LessonServiceException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public int updateLesson(String uuid, Map<String, Object> lessonRequestBody) {
        String identity = (String) lessonRequestBody.get("identity");
        String userId = (String) lessonRequestBody.get("userId");
        // 1. 查找课程
        Lesson lesson = lessonMapper.selectByUuid(uuid);
        if (lesson == null) {
            throw new LessonServiceException("课程不存在");
        }
        Long lessonId = lesson.getId();
        Long currentVersionId = lesson.getCurrentVersionId();

        // 2. 归档旧版本
        if (identity.equals("ADMIN")) {
            if (currentVersionId != null) {
                LessonVersion oldVersion = lessonVersionMapper.selectById(currentVersionId);
                if (oldVersion != null) {
                    oldVersion.setStatus("archived");
                    lessonVersionMapper.update(oldVersion);
                    // 归档后将其所有资源设为inactive
                    List<LessonResources> oldResources = lessonResourceMapper.selectByLessonVersionId(oldVersion.getId());
                    if (oldResources != null) {
                        for (LessonResources res : oldResources) {
                            res.setStatus("inactive");
                            lessonResourceMapper.update(res);
                        }
                    }
                }
            }
        }

        // 3. 新建新版本
        LessonVersion newVersion = new LessonVersion();
        newVersion.setUuid(UUID.randomUUID().toString());
        newVersion.setLessonId(lessonId);
        int newVersionNum = 1;
        if (currentVersionId != null) {
            LessonVersion oldVersion = lessonVersionMapper.selectById(currentVersionId);
            if (oldVersion != null && oldVersion.getVersion() != null) {
                newVersionNum = oldVersion.getVersion() + 1;
            }
        }

        newVersion.setVersion(newVersionNum);
        newVersion.setName((String) lessonRequestBody.get("name"));
        newVersion.setDescription((String) lessonRequestBody.get("description"));
        newVersion.setImageUrl((String) lessonRequestBody.get("imageUrl"));
        newVersion.setAuthorName((String) lessonRequestBody.get("authorName"));
        newVersion.setSortOrder(0);
        newVersion.setStatus("active");
        // 通过userMapper查主键id
        Long creatorId = null;
        if (userId != null) {
            if ("ADMIN".equals(identity)) {
                User user = userMapper.findByUuid(userId);
                if (user != null) creatorId = user.getId();
            } else if ("COMPANY".equals(identity)) {
                Company company = companyMapper.findByUuid(userId);
                if (company != null) creatorId = company.getId();
            }
        }
        newVersion.setCreatorId(creatorId);
        newVersion.setIsDeleted(0);
        newVersion.setCreatedAt(LocalDateTime.now());
        lessonVersionMapper.insert(newVersion);

        // 4. 插入新资源 - 适配新的请求体结构
        List<String> resourcesUrls = null;
        Object resourcesUrlsObj = lessonRequestBody.get("resourcesUrls");
        if (resourcesUrlsObj instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<String> tmp = (List<String>) resourcesUrlsObj;
            resourcesUrls = tmp;
        }
        
        String resourcesType = (String) lessonRequestBody.get("resourcesType");
        // 验证resourcesType是否为有效值
        if (resourcesType != null && !isValidResourceType(resourcesType)) {
            throw new LessonServiceException("无效的资源类型: " + resourcesType + "，允许的类型: video, audio, document, image, link, other");
        }
        
        List<Integer> sortOrders = null;
        Object sortOrdersObj = lessonRequestBody.get("sortOrders");
        if (sortOrdersObj instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<Integer> tmp = (List<Integer>) sortOrdersObj;
            sortOrders = tmp;
        }
        
        if (resourcesUrls != null && !resourcesUrls.isEmpty()) {
            for (int i = 0; i < resourcesUrls.size(); i++) {
                LessonResources resource = new LessonResources();
                resource.setUuid(UUID.randomUUID().toString());
                resource.setLessonVersionId(lessonVersionMapper.selectByUuid(newVersion.getUuid()).getId());
                resource.setResourcesUrl(resourcesUrls.get(i));
                resource.setResourcesType(resourcesType != null ? resourcesType : "other");
                // sortOrder与资源一一对应
                if (sortOrders != null && i < sortOrders.size() && sortOrders.get(i) != null) {
                    resource.setSortOrder(sortOrders.get(i));
                } else {
                    resource.setSortOrder(0);
                }
                resource.setStatus("active");
                resource.setIsDeleted(0);
                resource.setCreatedAt(LocalDateTime.now());
                resource.setUpdatedAt(LocalDateTime.now());
                lessonResourceMapper.insert(resource);
            }
        }

        // 5. 更新lesson指针
        if (identity.equals("ADMIN")) {
            lesson.setCurrentVersionId(lessonVersionMapper.selectByUuid(newVersion.getUuid()).getId());
            lesson.setStatus("active");
            lesson.setUpdatedAt(LocalDateTime.now());
            lessonMapper.update(lesson);
        } else if (identity.equals("COMPANY")) {
            lesson.setPendingVersionId(lessonVersionMapper.selectByUuid(newVersion.getUuid()).getId());
            lesson.setStatus("pending_review");
            lesson.setUpdatedAt(LocalDateTime.now());
            lessonMapper.update(lesson);
        }

        return 1;
    }

    @Override
    public int deleteLesson(List<String> uuids) {
        // 1. 查出所有lessonId
        List<Long> lessonIds = new ArrayList<>();
        for (String uuid : uuids) {
            Lesson lesson = lessonMapper.selectByUuid(uuid);
            if (lesson != null) lessonIds.add(lesson.getId());
        }
        if (lessonIds.isEmpty()) return 0;
        // 2. 软删除lesson
        lessonMapper.softDeleteLessons(lessonIds);
        // 3. 查出所有versionId
        List<LessonVersion> versions = new ArrayList<>();
        for (Long lessonId : lessonIds) {
            versions.addAll(lessonVersionMapper.selectAllByLessonId(lessonId));
        }
        List<Long> versionIds = new ArrayList<>();
        for (LessonVersion v : versions) {
            versionIds.add(v.getId());
        }
        // 4. 软删除version
        if (!lessonIds.isEmpty()) lessonVersionMapper.softDeleteVersionsByLessonIds(lessonIds);
        // 5. 软删除resource
        if (!versionIds.isEmpty()) lessonResourceMapper.softDeleteResourcesByVersionIds(versionIds);
        return lessonIds.size();
    }

    @Override
    public LessonListResponse getAllLessons(String name, String authorName, String beginTime, String endTime, int page, int size) {
        int offset = page * size;
        List<Map<String, Object>> rawList = lessonMapper.selectLessonWithCurrentVersion(name, authorName, beginTime, endTime, size, offset);
        List<LessonListItemResponse> resultList = new ArrayList<>();
        rawTravel(rawList, resultList);
        LessonListResponse resp = new LessonListResponse();
        resp.setTotal(resultList.size());
        resp.setList(resultList);
        return resp;
    }

    private void rawTravel(List<Map<String, Object>> rawList, List<LessonListItemResponse> resultList) {
        for (Map<String, Object> row : rawList) {
            LessonListItemResponse dto = new LessonListItemResponse();
            dto.setName((String) row.get("name"));
            dto.setImageUrl((String) row.get("image_url"));
            dto.setDescription((String) row.get("description"));
            dto.setAuthorName((String) row.get("author_name"));
            dto.setVersion(row.get("version") != null ? ((Number)row.get("version")).intValue() : null);
            dto.setUuid((String) row.get("uuid"));
            resultList.add(dto);
        }
    }

    @Override
    public LessonDetailResponse getLessonDetail(LessonDetailRequest req) {
        LessonDetailResponse resp = new LessonDetailResponse();
        String uuid = req.getUuid();
        int page = req.getPage() != null ? req.getPage() : 0;
        int size = req.getSize() != null ? req.getSize() : 10;
        int offset = page * size;
        // 查课程
        Lesson lesson = lessonMapper.selectByUuid(uuid);
        Long lessonId = lesson.getId();
        Long currentVersionId = lesson.getCurrentVersionId();
        LessonVersion version = null;
        if (currentVersionId != null) {
            version = lessonVersionMapper.selectById(currentVersionId);
        }
        if (version != null) {
            resp.setName(version.getName());
            resp.setImageUrl(version.getImageUrl());
            resp.setDescription(version.getDescription());
            resp.setAuthorName(version.getAuthorName());
            resp.setVersion(version.getVersion());
            resp.setVersionDescription(version.getDescription());
            // 查资源分页，按sort_order排序
            List<LessonResources> allResources = lessonResourceMapper.selectByLessonVersionId(version.getId());
            allResources.sort(Comparator.comparingInt(LessonResources::getSortOrder));
            int total = allResources.size();
            resp.setTotal(total);
            List<LessonResourceItemResponse> resourceList = new ArrayList<>();
            for (int i = offset; i < Math.min(offset + size, total); i++) {
                LessonResources res = allResources.get(i);
                LessonResourceItemResponse dto = new LessonResourceItemResponse();
                dto.setResourcesType(res.getResourcesType());
                dto.setResourcesUrl(res.getResourcesUrl());
                resourceList.add(dto);
            }
            resp.setResources(resourceList);
        }
        return resp;
    }

    @Override
    public int approveLesson(String uuid, Map<String, Object> approvalBody) {
        // 1. 查找课程
        Lesson lesson = lessonMapper.selectByUuid(uuid);
        if (lesson == null) return -1;
        Long pendingVersionId = lesson.getPendingVersionId();
        if (pendingVersionId == null) return -1;
        LessonVersion pendingVersion = lessonVersionMapper.selectById(pendingVersionId);
        if (pendingVersion == null) return -1;
        String result = (String) approvalBody.get("auditStatus"); // "approved" or "rejected"
        String comments = (String) approvalBody.get("comment");
        // 2. 审批通过
        Long auditorId = approvalBody.get("auditorId") != null ? ((Number)approvalBody.get("auditorId")).longValue() : null;
        if ("approved".equalsIgnoreCase(result)) {
            // 归档原currentVersion
            Long currentVersionId = lesson.getCurrentVersionId();
            if (currentVersionId != null) {
                LessonVersion oldVersion = lessonVersionMapper.selectById(currentVersionId);
                if (oldVersion != null) {
                    oldVersion.setStatus("archived");
                    lessonVersionMapper.update(oldVersion);
                    // 归档后将其所有资源设为inactive
                    List<LessonResources> oldResources = lessonResourceMapper.selectByLessonVersionId(oldVersion.getId());
                    if (oldResources != null) {
                        for (LessonResources res : oldResources) {
                            res.setStatus("inactive");
                            lessonResourceMapper.update(res);
                        }
                    }
                }
            }
            // 更新pendingVersion为active
            pendingVersion.setStatus("active");
            lessonVersionMapper.update(pendingVersion);
            // 更新lesson指针
            lesson.setCurrentVersionId(pendingVersionId);
            lesson.setPendingVersionId(null);
            lesson.setStatus("active");
            lesson.setUpdatedAt(java.time.LocalDateTime.now());
            lessonMapper.update(lesson);
            // 通知发布者
            sendLessonApprovalNotification(lesson, pendingVersion, true, comments);;
            // 插入审批历史
            insertLessonAuditHistory(pendingVersionId, auditorId, "approved", comments);
            return 1;
        } else if ("rejected".equalsIgnoreCase(result)) {
            // 更新pendingVersion为rejected
            pendingVersion.setStatus("rejected");
            lessonVersionMapper.update(pendingVersion);
            // lesson pendingVersionId设为null，状态archived
            lesson.setPendingVersionId(null);
            lesson.setStatus("archived");
            lesson.setUpdatedAt(LocalDateTime.now());
            lessonMapper.update(lesson);
            // 通知发布者
            sendLessonApprovalNotification(lesson, pendingVersion, false, comments);
            // 插入审批历史
            insertLessonAuditHistory(pendingVersionId, auditorId, "rejected", comments);
            return 1;
        }
        return -1;
    }

    // 审批通知辅助方法
    private void sendLessonApprovalNotification(Lesson lesson, LessonVersion version, boolean approved, String comments) {
        String title = approved ? "课程审核通过" : "课程审核未通过";
        String content = approved ?
                String.format("您的课程《%s》已通过审核。", version.getName()) :
                String.format("您的课程《%s》未通过审核，原因：%s", version.getName(), comments != null ? comments : "无");
        // 判断发布者是公司还是管理员
        if (lesson.getPublisherId() != null) {
            // 这里假设publisherId为公司id，实际可根据业务调整
            notificationService.sendSystemNotificationToUser(lesson.getPublisherId(), title, content, RelatedObjectType.LESSON, lesson.getId());
        }
    }

    @Override
    public LessonSearchResponse searchLesson(LessonSearchRequest req) {
        String keyword = req.getKeyWord();
        int page = req.getPage() != null ? req.getPage() : 0;
        int size = req.getSize() != null ? req.getSize() : 10;
        int offset = page * size;
        List<Map<String, Object>> rawList = lessonMapper.searchLessonWithCurrentVersion(keyword, size, offset);
        List<LessonListItemResponse> resultList = new java.util.ArrayList<>();
        rawTravel(rawList, resultList);
        LessonSearchResponse resp = new LessonSearchResponse();
        resp.setTotal(resultList.size());
        resp.setList(resultList);
        return resp;
    }

    @Override
    public List<Map<String, Object>> getReviewLessonsWithPendingVersion(int pageSize, int offset) {
        return lessonMapper.selectReviewLessonsWithPendingVersion(pageSize, offset);
    }

    // 审批历史插入辅助方法
    private void insertLessonAuditHistory(Long lessonVersionId, Long auditorId, String auditStatus, String comments) {
        LessonAuditHistory history = new LessonAuditHistory();
        history.setLessonVersionId(lessonVersionId != null ? lessonVersionId.toString() : null);
        history.setAuditorId(auditorId);
        history.setAuditStatus(auditStatus);
        history.setComments(comments);
        history.setIsDeleted(0);
        history.setCreatedAt(java.time.LocalDateTime.now());
        history.setUuid(java.util.UUID.randomUUID().toString());
        lessonAuditHistoryMapper.insert(history);
    }

    @Override
    public Map<String, Object> getLessonAuditHistoryPage(String auditStatus, String beginTime, String endTime, int page, int size) {
        int offset = page * size;
        List<LessonAuditHistory> list = lessonAuditHistoryMapper.selectHistoryPage(auditStatus, beginTime, endTime, size, offset);
        // 查询总数
        int total = lessonAuditHistoryMapper.countHistory(auditStatus, beginTime, endTime);
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", list);
        return result;
    }

    @Override
    public int softDeleteLessonAuditHistory(List<String> uuids) {
        if (uuids == null || uuids.isEmpty()) return 0;
        return lessonAuditHistoryMapper.softDeleteHistoryByUuids(uuids);
    }
    
    /**
     * 验证资源类型是否为数据库ENUM允许的值
     * @param resourceType 资源类型
     * @return 是否为有效值
     */
    private boolean isValidResourceType(String resourceType) {
        if (resourceType == null) return false;
        return resourceType.equals("video") || 
               resourceType.equals("audio") || 
               resourceType.equals("document") || 
               resourceType.equals("image") || 
               resourceType.equals("link") || 
               resourceType.equals("other");
    }
} 