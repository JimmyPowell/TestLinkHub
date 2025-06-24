package tech.cspioneer.backend.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingAuditHistory {
    private Long id;
    private Long meetingVersionId;
    private Long auditorId;
    private String auditStatus;
    private String comments;
    private LocalDateTime createdAt;
}
