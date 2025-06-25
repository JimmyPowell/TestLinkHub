package tech.cspioneer.backend.entity.dto.request;

import lombok.Data;

@Data
public class NewsQueryRequest {
    /**
     * 新闻最晚时间
     */
    private String endTime;
    /**
     * 新闻最早时间
     */
    private String startTime;
    /**
     * 新闻摘要
     */
    private String summary;
    /**
     * 新闻标题
     */
    private String title;
}