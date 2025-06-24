package tech.cspioneer.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.cspioneer.backend.entity.enums.NotificationType;
import tech.cspioneer.backend.entity.enums.RelatedObjectType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    private Long id;

    private String uuid;

    private Long senderId;

    private String title;

    private String content;

    private NotificationType type;

    private RelatedObjectType relatedObjectType;

    private Long relatedObjectId;

    private LocalDateTime createdAt;
}
