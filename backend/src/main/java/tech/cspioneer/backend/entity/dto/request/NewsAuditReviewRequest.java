package tech.cspioneer.backend.entity.dto.request;

import lombok.Data;

@Data
public class NewsAuditReviewRequest {
    /**
     * 审核后的状态
     */
    private String auditStatus;
    /**
     * 通知语句
     */
    private String comment;
}