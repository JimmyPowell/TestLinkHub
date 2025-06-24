package tech.cspioneer.backend.service;

import org.springframework.data.domain.Page;
import tech.cspioneer.backend.entity.dto.response.NotificationResponse;
import tech.cspioneer.backend.entity.enums.RelatedObjectType;

import java.util.List;

public interface NotificationService {

    /**
     * 根据用户UUID和状态分页获取通知列表
     *
     * @param userUuid 用户UUID
     * @param status   状态 ("all", "read", "unread")
     * @param page     页码 (从0开始)
     * @param size     每页数量
     * @return 分页后的通知列表
     */
    Page<NotificationResponse> getNotificationsByUserUuid(String userUuid, String status, int page, int size);

    /**
     * 获取单条通知详情
     *
     * @param notificationUuid 通知的UUID
     * @param userUuid         用户UUID，用于校验权限
     * @return 通知详情
     */
    NotificationResponse getNotificationDetails(String notificationUuid, String userUuid);

    /**
     * 将单条通知标记为已读
     *
     * @param notificationUuid 通知的UUID
     * @param userUuid         用户UUID
     */
    void markNotificationAsRead(String notificationUuid, String userUuid);

    /**
     * 批量将通知标记为已读
     *
     * @param notificationUuids 通知的UUID列表
     * @param userUuid          用户UUID
     */
    void markNotificationsAsRead(List<String> notificationUuids, String userUuid);

    /**
     * 将所有未读通知标记为已读
     *
     * @param userUuid 用户UUID
     */
    void markAllNotificationsAsRead(String userUuid);

    /**
     * 删除（软删除）一条通知
     *
     * @param notificationUuid 通知的UUID
     * @param userUuid         用户UUID
     */
    void deleteNotification(String notificationUuid, String userUuid);

    // --- Internal System-Facing Methods ---

    /**
     * 向单个用户发送系统通知
     * @param recipientId 接收用户ID
     * @param title 标题
     * @param content 内容
     * @param objectType 关联对象类型
     * @param objectId 关联对象ID
     */
    void sendSystemNotificationToUser(Long recipientId, String title, String content, RelatedObjectType objectType, Long objectId);

    /**
     * 向公司所有员工发送系统通知
     * @param companyId 公司ID
     * @param title 标题
     * @param content 内容
     * @param objectType 关联对象类型
     * @param objectId 关联对象ID
     */
    void sendSystemNotificationToCompany(Long companyId, String title, String content, RelatedObjectType objectType, Long objectId);

    /**
     * 向所有用户发送系统通知 (广播)
     * @param title 标题
     * @param content 内容
     * @param objectType 关联对象类型
     * @param objectId 关联对象ID
     */
    void sendSystemNotificationToAll(String title, String content, RelatedObjectType objectType, Long objectId);

    // --- Admin-Facing Methods ---

    /**
     * [ADMIN] 获取所有通知主体的分页列表
     * @param page 页码
     * @param size 每页数量
     * @return 通知分页对象
     */
    Page<tech.cspioneer.backend.entity.Notification> getAllNotifications(int page, int size);

    /**
     * 向会议所有参会者发送系统通知
     * @param meetingId 会议ID
     * @param title 标题
     * @param content 内容
     */
    void sendSystemNotificationToMeetingParticipants(Long meetingId, String title, String content);
}
