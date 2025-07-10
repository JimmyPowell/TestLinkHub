package tech.cspioneer.backend.entity.query;

import lombok.Data;

@Data
public class NewsListQuery {
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
    /**
     * 新闻页数
     */
    private int page;
    /**
     * 新闻页大小
     */
    private int pageSize;
    /**
     * 查询排序
     */
    private String orderBy;
    /**
     * 查询公司ID
     */
    private String companyUuid;
}
