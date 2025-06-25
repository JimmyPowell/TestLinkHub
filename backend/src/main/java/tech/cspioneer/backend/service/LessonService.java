package tech.cspioneer.backend.service;

import java.util.List;
import java.util.Map;

public interface LessonService {
    // 课程上传
    int uploadLesson(Map<String, Object> lessonRequestBody);

    // 课程修改
    int updateLesson(String uuid, Map<String, Object> lessonRequestBody);

    // 课程删除
    int deleteLesson(List<String> uuids);

    // 课程分页查询
    Map<String, Object> findLesson(Map<String, Object> searchBody);

    // 课程浏览（分页）
    Map<String, Object> getAllLessons(Map<String, Object> pageBody);

    // 课程详情
    Map<String, Object> getLessonDetail(String uuid);

    // 审核列表
    Map<String, Object> getLessonReviewHistory(Map<String, Object> pageBody);

    // 审核操作
    int approveLesson(String uuid, Map<String, Object> approvalBody);

    // 审核历史
    Map<String, Object> getLessonReviewList(Map<String, Object> pageBody);

    // 删除审核历史
    int deleteLessonReviewHistory(List<String> uuids);
}