package tech.cspioneer.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.cspioneer.backend.entity.*;
import tech.cspioneer.backend.entity.dto.response.*;
import tech.cspioneer.backend.mapper.*;
import tech.cspioneer.backend.service.LessonService;
import tech.cspioneer.backend.exception.LessonServiceException;
import tech.cspioneer.backend.entity.dto.request.LessonDetailRequest;
import tech.cspioneer.backend.entity.dto.request.LessonSearchRequest;

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

    @Override
    @Transactional
    public int uploadLesson(Map<String, Object> lessonRequestBody) {
        String identity = (String) lessonRequestBody.get("identity");
        String uuid = (String) lessonRequestBody.get("userId");
        try {
            // 1. 创建课程资源（支持多个资源）
            Object resObj = lessonRequestBody.get("resources");
            List<Map<String, Object>> resources = null;
            if (resObj instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> tmp = (List<Map<String, Object>>) resObj;
                resources = tmp;
            }
            List<Integer> sortOrders = null;
            Object sortOrdersObj = lessonRequestBody.get("sort_orders");
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
                lesson.setPendingVersionId(Long.valueOf(version.getVersion()));
                lessonMapper.insert(lesson);
                Lesson newLesson = lessonMapper.selectByUuid(lesson.getUuid());
                version.setLessonId(newLesson.getId());
                version.setCreatorId(company.getId());
                version.setStatus("pending_review");
                lessonVersionMapper.insert(version);
                System.out.println(2);
            } else if ("ADMIN".equals(identity)) {
                User user = userMapper.findByUuid(uuid);
                lesson.setPublisherId(user.getId());
                lesson.setStatus("active");
                lesson.setCurrentVersionId(Long.valueOf(version.getVersion()));
                lesson.setPendingVersionId(null);
                lessonMapper.insert(lesson);
                Lesson newLesson = lessonMapper.selectByUuid(lesson.getUuid());
                version.setLessonId(newLesson.getId());
                version.setCreatorId(user.getId());
                version.setStatus("active");
                lessonVersionMapper.insert(version);
            } else {
                return -1; // 其他身份不允许
            }
            if (resources != null) {
                for (int i = 0; i < resources.size(); i++) {
                    Map<String, Object> res = resources.get(i);
                    LessonResources resource = new LessonResources();
                    resource.setUuid(UUID.randomUUID().toString());
                    resource.setLessonVersionId(version.getId());
                    resource.setResourcesUrl((String) res.get("resourcesUrl"));
                    resource.setResourcesType((String) res.get("resourcesType"));
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

        // 4. 插入新资源
        Object resObj = lessonRequestBody.get("resources");
        List<Map<String, Object>> resources = null;
        if (resObj instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tmp = (List<Map<String, Object>>) resObj;
            resources = tmp;
        }
        List<Integer> sortOrders = null;
        Object sortOrdersObj = lessonRequestBody.get("sort_orders");
        if (sortOrdersObj instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<Integer> tmp = (List<Integer>) sortOrdersObj;
            sortOrders = tmp;
        }
        if (resources != null) {
            for (int i = 0; i < resources.size(); i++) {
                Map<String, Object> res = resources.get(i);
                LessonResources resource = new LessonResources();
                resource.setUuid(UUID.randomUUID().toString());
                resource.setLessonVersionId(lessonVersionMapper.selectByUuid(newVersion.getUuid()).getId());
                resource.setResourcesUrl((String) res.get("resourcesUrl"));
                resource.setResourcesType((String) res.get("resourcesType"));
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
            lesson.setStatus("archived");
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
    public Map<String, Object> getLessonReviewHistory(Map<String, Object> pageBody) {
        return Map.of();
    }

    @Override
    public int approveLesson(String uuid, Map<String, Object> approvalBody) {
        return 0;
    }

    @Override
    public Map<String, Object> getLessonReviewList(Map<String, Object> pageBody) {
        return Map.of();
    }

    @Override
    public int deleteLessonReviewHistory(List<String> uuids) {
        return 0;
    }

    @Override
    public LessonSearchResponse searchLesson(LessonSearchRequest req) {
        String keyword = req.getKeyword();
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
} 