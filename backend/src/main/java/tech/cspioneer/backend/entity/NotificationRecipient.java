package tech.cspioneer.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRecipient {

    private Long id;

    private Long notificationId;

    private Long recipientId;

    private boolean isRead;

    private LocalDateTime readAt;

    private boolean isDeleted;

    private LocalDateTime createdAt;
}


