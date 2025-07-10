package tech.cspioneer.backend.entity.dto.request;

import lombok.Data;

/**
 * 课程搜索查询请求体
 */
@Data
public class LessonSearchQueryRequest {
    /**
     * 每页大小
     */
    private Integer size;
    
    /**
     * 页码
     */
    private Integer page;
    
    /**
     * 搜索关键词
     */
    private String keyWord;
} 