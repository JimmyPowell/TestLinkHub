package tech.cspioneer.backend.entity.dto.request;

import lombok.Data;

@Data

public class NewsUploadRequest {
    /**
     * 发布公司ID
     */
    private String companyUuid;
    /**
     * 封面图片URL
     */
    private String coverImageUrl;
    /**
     * 此版本内容的创建/修改者ID
     */
    private String publisherUuid;
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
     * 0:仅企业内部可见, 1:全部用户可见
     */
    private int visible;
    /**
     * 操作人员UUid
     */
    private String UserUUid;
    /**
     * 操作人员权限
     */
    private String identity;

}