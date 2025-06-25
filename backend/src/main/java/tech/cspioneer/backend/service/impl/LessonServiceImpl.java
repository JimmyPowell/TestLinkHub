package tech.cspioneer.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.cspioneer.backend.entity.*;
import tech.cspioneer.backend.mapper.*;
import tech.cspioneer.backend.service.LessonService;
import tech.cspioneer.backend.exception.LessonServiceException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
                lesson.setCurrentVersionId(Long.valueOf(version.getVersion()));
                lesson.setPendingVersionId(null);
                lessonMapper.insert(lesson);
                Lesson newLesson = lessonMapper.selectByUuid(lesson.getUuid());
                version.setLessonId(newLesson.getId());
                version.setCreatorId(company.getId());
                version.setStatus("pending_review");
                System.out.println(version);
                lessonVersionMapper.insert(version);
                System.out.println(2);
            } else if ("ADMIN".equals(identity)) {
                User user = userMapper.findByUuid(uuid);
                lesson.setPublisherId(user.getId());
                lesson.setStatus("active");
                lesson.setCurrentVersionId(null);
                lesson.setPendingVersionId(Long.valueOf(version.getVersion()));
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
    public int updateLesson(String uuid, Map<String, Object> lessonRequestBody) {
        return 0;
    }

    @Override
    public int deleteLesson(List<String> uuids) {
        return 0;
    }

    @Override
    public Map<String, Object> findLesson(Map<String, Object> searchBody) {
        return Map.of();
    }

    @Override
    public Map<String, Object> getAllLessons(Map<String, Object> pageBody) {
        return Map.of();
    }

    @Override
    public Map<String, Object> getLessonDetail(String uuid) {
        return Map.of();
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
} 