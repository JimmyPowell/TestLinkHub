package tech.cspioneer.backend.entity.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NewsAuditDetailResponse {
    /**
     * 发布公司ID
     */
    private long companyId;
    /**
     * 该版本创建时间
     */
    private String contentCreatedAt;
    /**
     * 封面图片URL
     */
    private String coverImageUrl;
    /**
     * 版本创建时间
     */
    private String createdAt;
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
     * 新闻内容版本唯一标识符
     */
    private String uuid;
    /**
     * 该版本的版本号
     */
    private long version;
}
