package tech.cspioneer.backend.entity.dto.response;

import lombok.Data;
import tech.cspioneer.backend.entity.enums.NewsAuditStatus;
@Data
public class NewsHistoryResponse {
    /**
     * 审核结果
     */
    private NewsAuditStatus auditStatus;
    /**
     * 审核员UUID
     */
    private String auditorUUid;
    /**
     * 审核意见或备注
     */
    private String comments;
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
     * 审核创建时间
     */
    private String createdAt;
    /**
     * 被审核的新闻内容版本UUID
     */
    private String newsContentUUid;
    /**
     * 此版本内容的创建/修改者ID
     */
    private long publisherId;
    /**
     * 新闻摘要
     */
    private String summary;
    /**
     * 新闻标题
     */
    private String title;
    /**
     * 该新闻历史的UUID
     */
    private String uuid;
    /**
     * 该版本的版本号
     */
    private long version;
}
