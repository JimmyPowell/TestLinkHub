package tech.cspioneer.backend.entity.dto.request;

import lombok.Data;

/**
 * 课程详情查询请求体
 */
@Data
public class LessonDetailQueryRequest {
    /**
     * 课程UUID
     */
    private String uuid;
    
    /**
     * 页码
     */
    private Integer page;
    
    /**
     * 每页大小
     */
    private Integer size;
} 