package tech.cspioneer.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.cspioneer.backend.entity.enums.NewsAuditHistoryStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsAuditHistory {
    private Long id;
    private Long newsContentId;
    private Long auditorId;
    private NewsAuditHistoryStatus auditStatus;
    private String comments;
    private int isDeleted;
    private LocalDateTime createdAt;
}
