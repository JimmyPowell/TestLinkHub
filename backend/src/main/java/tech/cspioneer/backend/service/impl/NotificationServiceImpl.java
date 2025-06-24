package tech.cspioneer.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.cspioneer.backend.entity.Notification;
import tech.cspioneer.backend.entity.NotificationRecipient;
import tech.cspioneer.backend.entity.enums.RelatedObjectType;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.entity.dto.response.NotificationResponse;
import tech.cspioneer.backend.exception.ResourceNotFoundException;
import tech.cspioneer.backend.mapper.MeetingParticipantMapper;
import tech.cspioneer.backend.mapper.NotificationMapper;
import tech.cspioneer.backend.mapper.UserMapper;
import tech.cspioneer.backend.service.NotificationService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final MeetingParticipantMapper meetingParticipantMapper;

    @Override
    public Page<NotificationResponse> getNotificationsByUserUuid(String userUuid, String status, int page, int size) {
        User user = getUserByUuid(userUuid);
        PageRequest pageRequest = PageRequest.of(page, size);

        Boolean isRead = switch (status.toLowerCase()) {
            case "read" -> true;
            case "unread" -> false;
            default -> null; // "all"
        };

        long total = notificationMapper.countNotificationsByRecipientId(user.getId(), isRead);
        List<NotificationResponse> notifications = notificationMapper.findNotificationsByRecipientId(
                user.getId(), isRead, (int) pageRequest.getOffset(), pageRequest.getPageSize());

        return new PageImpl<>(notifications, pageRequest, total);
    }

    @Override
    public NotificationResponse getNotificationDetails(String notificationUuid, String userUuid) {
        // This method can be improved by adding a direct mapper query
        // that fetches the details and verifies ownership in one go.
        // For now, we do it in two steps.
        User user = getUserByUuid(userUuid);
        Notification notification = getNotificationByUuid(notificationUuid);

        // A more efficient query could do this in the database.
        Page<NotificationResponse> userNotifications = getNotificationsByUserUuid(userUuid, "all", 0, 1);
        return userNotifications.stream()
                .filter(n -> n.getUuid().equals(notificationUuid))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found or you do not have permission to view it."));
    }

    @Override
    @Transactional
    public void markNotificationAsRead(String notificationUuid, String userUuid) {
        User user = getUserByUuid(userUuid);
        Notification notification = getNotificationByUuid(notificationUuid);
        notificationMapper.markAsRead(notification.getId(), user.getId());
    }

    @Override
    @Transactional
    public void markNotificationsAsRead(List<String> notificationUuids, String userUuid) {
        User user = getUserByUuid(userUuid);
        // This is not the most efficient way for batch operations.
        // A single UPDATE query with a WHERE IN clause would be better.
        // For simplicity, we iterate here.
        for (String uuid : notificationUuids) {
            Notification notification = getNotificationByUuid(uuid);
            notificationMapper.markAsRead(notification.getId(), user.getId());
        }
    }

    @Override
    @Transactional
    public void markAllNotificationsAsRead(String userUuid) {
        User user = getUserByUuid(userUuid);
        notificationMapper.markAllAsRead(user.getId());
    }



    @Override
    @Transactional
    public void deleteNotification(String notificationUuid, String userUuid) {
        User user = getUserByUuid(userUuid);
        Notification notification = getNotificationByUuid(notificationUuid);
        notificationMapper.softDelete(notification.getId(), user.getId());
    }

    @Override
    @Transactional
    public void sendSystemNotificationToUser(Long recipientId, String title, String content, RelatedObjectType objectType, Long objectId) {
        List<Long> recipientIds = Collections.singletonList(recipientId);
        createAndDistributeNotification(recipientIds, title, content, objectType, objectId);
    }

    @Async("taskExecutor") // 使用自定义的线程池执行异步任务
    @Override
    @Transactional
    public void sendSystemNotificationToCompany(Long companyId, String title, String content, RelatedObjectType objectType, Long objectId) {
        List<Long> recipientIds = userMapper.findActiveUserIdsByCompanyId(companyId);
        createAndDistributeNotification(recipientIds, title, content, objectType, objectId);
    }

    @Async("taskExecutor") // 使用自定义的线程池执行异步任务
    @Override
    @Transactional
    public void sendSystemNotificationToAll(String title, String content, RelatedObjectType objectType, Long objectId) {
        List<Long> recipientIds = userMapper.findAllActiveUserIds();
        createAndDistributeNotification(recipientIds, title, content, objectType, objectId);
    }

    // --- Private Helper Methods ---

    private User getUserByUuid(String uuid) {
        User user = userMapper.findByUuid(uuid);
        if (user == null) {
            throw new ResourceNotFoundException("User", "uuid", uuid);
        }
        return user;
    }

    private Notification getNotificationByUuid(String uuid) {
        Notification notification = notificationMapper.findNotificationByUuid(uuid);
        if (notification == null) {
            throw new ResourceNotFoundException("Notification", "uuid", uuid);
        }
        return notification;
    }

    /**
     * 内部核心方法：创建通知并分发给指定的接收者列表
     */
    private void createAndDistributeNotification(List<Long> recipientIds, String title, String content, RelatedObjectType objectType, Long objectId) {
        if (recipientIds == null || recipientIds.isEmpty()) {
            return; // 没有接收者，直接返回
        }

        // 1. 创建通知主体
        Notification notification = Notification.builder()
                .uuid(UUID.randomUUID().toString())
                .senderId(null) // null 表示系统发送
                .title(title)
                .content(content)
                .type(null) // 可根据业务细化
                .relatedObjectType(objectType)
                .relatedObjectId(objectId)
                .createdAt(LocalDateTime.now())
                .build();
        notificationMapper.insertNotification(notification);

        // 2. 创建接收人列表
        List<NotificationRecipient> recipients = new ArrayList<>();
        for (Long recipientId : recipientIds) {
            NotificationRecipient recipient = NotificationRecipient.builder()
                    .notificationId(notification.getId())
                    .recipientId(recipientId)
                    .isRead(false)
                    .isDeleted(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            recipients.add(recipient);
        }

        // 3. 批量插入接收人记录
        // 在实际生产中，如果recipients列表非常大，需要考虑分批插入
        notificationMapper.batchInsertNotificationRecipients(recipients);
    }

    @Override
    public Page<Notification> getAllNotifications(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        long total = notificationMapper.countAllNotifications();
        List<Notification> notifications = notificationMapper.findAllNotifications((int) pageRequest.getOffset(), pageRequest.getPageSize());
        return new PageImpl<>(notifications, pageRequest, total);
    }

    @Async("taskExecutor")
    @Override
    @Transactional
    public void sendSystemNotificationToMeetingParticipants(Long meetingId, String title, String content) {
        List<Long> recipientIds = meetingParticipantMapper.findUserIdsByMeetingId(meetingId);
        createAndDistributeNotification(recipientIds, title, content, RelatedObjectType.MEETING, meetingId);
    }
}
