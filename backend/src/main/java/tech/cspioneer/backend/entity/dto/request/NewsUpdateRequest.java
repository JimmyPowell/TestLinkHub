package tech.cspioneer.backend.entity.dto.request;

import lombok.Data;

@Data
public class NewsUpdateRequest {
    /**
     * 封面图片URL
     */
    private String coverImageUrl;
    /**
     * 是否删除 0-未删除 1-已删除
     */
    private long isDeleted;
    /**
     * 此版本内容的创建/修改者ID
     */
    private long publisherId;
    /**
     * 新闻正文内容 (HTML或Markdown)
     */
    private String resourceUrl;
    /**
     * 新闻摘要
     */
    private String summary;
    /**
     * 新闻标题
     */
    private String title;
    /**
     * 是否可见（0:仅企业内部可见, 1:全部用户可见）
     */
    private Long visible;
    private String userUUid;
    private String identity;
}