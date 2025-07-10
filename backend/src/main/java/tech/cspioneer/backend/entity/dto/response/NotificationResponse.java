package tech.cspioneer.backend.entity.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import tech.cspioneer.backend.entity.enums.NotificationType;
import tech.cspioneer.backend.entity.enums.RelatedObjectType;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponse {

    private String uuid;

    private String title;

    private String content;

    private NotificationType type;

    private RelatedObjectType relatedObjectType;
    
    private String relatedObjectUuid; //暴露关联对象的UUID

    private LocalDateTime createdAt;

    private boolean isRead;

    private LocalDateTime readAt;
}
