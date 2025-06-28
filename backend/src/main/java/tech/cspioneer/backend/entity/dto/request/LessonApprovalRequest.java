package tech.cspioneer.backend.entity.dto.request;

import lombok.Data;

/**
 * 课程审核请求体
 */
@Data
public class LessonApprovalRequest {
    /**
     * 审核状态
     */
    private String auditStatus;
    
    /**
     * 审核意见
     */
    private String comment;
} 