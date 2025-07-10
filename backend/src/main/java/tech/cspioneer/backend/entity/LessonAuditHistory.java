package tech.cspioneer.backend.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonAuditHistory {
    private Long id;
    private String lessonVersionId;
    private Long auditorId;
    private String auditStatus; // approved, rejected
    private String comments;
    private Integer isDeleted;
    private LocalDateTime createdAt;
    private String uuid;
}
