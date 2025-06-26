package tech.cspioneer.backend.service;

import java.util.List;
import java.util.Map;
import tech.cspioneer.backend.entity.dto.response.LessonListResponse;
import tech.cspioneer.backend.entity.dto.request.LessonDetailRequest;
import tech.cspioneer.backend.entity.dto.response.LessonDetailResponse;
import tech.cspioneer.backend.entity.dto.request.LessonSearchRequest;
import tech.cspioneer.backend.entity.dto.response.LessonSearchResponse;

public interface  LessonService {
    // 课程上传
    int uploadLesson(Map<String, Object> lessonRequestBody);

    // 课程修改
    int updateLesson(String uuid, Map<String, Object> lessonRequestBody);

    // 课程删除
    int deleteLesson(List<String> uuids);

    // 课程浏览（分页）
    LessonListResponse getAllLessons(String name, String authorName, String beginTime, String endTime, int page, int size);

    // 课程详情
    LessonDetailResponse getLessonDetail(LessonDetailRequest req);

    // 审核列表
    Map<String, Object> getLessonReviewHistory(Map<String, Object> pageBody);

    // 审核操作
    int approveLesson(String uuid, Map<String, Object> approvalBody);

    // 审核历史
    Map<String, Object> getLessonReviewList(Map<String, Object> pageBody);

    // 删除审核历史
    int deleteLessonReviewHistory(List<String> uuids);

    // 课程搜索
    LessonSearchResponse searchLesson(LessonSearchRequest req);
}