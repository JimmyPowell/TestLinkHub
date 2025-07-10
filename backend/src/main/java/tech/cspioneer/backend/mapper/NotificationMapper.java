package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.Notification;
import tech.cspioneer.backend.entity.NotificationRecipient;
import tech.cspioneer.backend.entity.dto.response.NotificationResponse;

import java.util.List;

@Mapper
public interface NotificationMapper {

    /**
     * 插入通知主体
     * @param notification 通知对象
     * @return 受影响的行数
     */
    @Insert("INSERT INTO notification(uuid, sender_id, title, content, type, related_object_type, related_object_id, created_at) " +
            "VALUES(#{uuid}, #{senderId}, #{title}, #{content}, #{type}, #{relatedObjectType}, #{relatedObjectId}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertNotification(Notification notification);

    /**
     * 批量插入通知接收人
     * @param recipients 接收人列表
     * @return 受影响的行数
     */
    @Insert("<script>" +
            "INSERT INTO notification_recipient(notification_id, recipient_id, is_read, read_at, is_deleted, created_at) VALUES " +
            "<foreach collection='list' item='item' index='index' separator=','>" +
            "(#{item.notificationId}, #{item.recipientId}, #{item.isRead}, #{item.readAt}, #{item.isDeleted}, #{item.createdAt})" +
            "</foreach>" +
            "</script>")
    int batchInsertNotificationRecipients(@Param("list") List<NotificationRecipient> recipients);

    /**
     * 根据用户ID分页查询通知
     * @param recipientId 接收用户ID
     * @param offset 分页偏移量
     * @param limit 每页数量
     * @return 通知响应对象列表
     */
    @Select("<script>" +
            "SELECT " +
            "n.uuid, n.title, n.content, n.type, n.related_object_type, n.created_at, " +
            "nr.is_read, nr.read_at, " +
            "CASE " +
            "  WHEN n.related_object_type = 'LESSON' THEN l.uuid " +
            "  WHEN n.related_object_type = 'NEWS' THEN nw.uuid " +
            "  WHEN n.related_object_type = 'MEETING' THEN m.uuid " +
            "  WHEN n.related_object_type = 'USER' THEN u.uuid " +
            "  WHEN n.related_object_type = 'COMPANY' THEN c.uuid " +
            "  ELSE NULL " +
            "END AS relatedObjectUuid " +
            "FROM notification n " +
            "JOIN notification_recipient nr ON n.id = nr.notification_id " +
            "LEFT JOIN lesson l ON n.related_object_type = 'LESSON' AND n.related_object_id = l.id " +
            "LEFT JOIN news nw ON n.related_object_type = 'NEWS' AND n.related_object_id = nw.id " +
            "LEFT JOIN meeting m ON n.related_object_type = 'MEETING' AND n.related_object_id = m.id " +
            "LEFT JOIN `user` u ON n.related_object_type = 'USER' AND n.related_object_id = u.id " +
            "LEFT JOIN company c ON n.related_object_type = 'COMPANY' AND n.related_object_id = c.id " +
            "WHERE nr.recipient_id = #{recipientId} AND nr.is_deleted = 0 " +
            "<if test='isRead != null'> AND nr.is_read = #{isRead} </if>" +
            "ORDER BY n.created_at DESC " +
            "LIMIT #{limit} OFFSET #{offset}" +
            "</script>")
    @Results({
            @Result(property = "uuid", column = "uuid"),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "type", column = "type"),
            @Result(property = "relatedObjectType", column = "related_object_type"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "isRead", column = "is_read"),
            @Result(property = "readAt", column = "read_at"),
            @Result(property = "relatedObjectUuid", column = "relatedObjectUuid")
    })
    List<NotificationResponse> findNotificationsByRecipientId(@Param("recipientId") Long recipientId, @Param("isRead") Boolean isRead, @Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 根据用户ID和读取状态统计通知总数
     * @param recipientId 接收用户ID
     * @param isRead 读取状态 (true: 已读, false: 未读, null: 全部)
     * @return 通知总数
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM notification_recipient " +
            "WHERE recipient_id = #{recipientId} AND is_deleted = 0" +
            "<if test='isRead != null'>" +
            "  AND is_read = #{isRead}" +
            "</if>" +
            "</script>")
    Long countNotificationsByRecipientId(@Param("recipientId") Long recipientId, @Param("isRead") Boolean isRead);

    /**
     * 根据用户ID统计未读通知数
     * @param recipientId 接收用户ID
     * @return 未读通知数
     */
    @Select("SELECT COUNT(*) FROM notification_recipient WHERE recipient_id = #{recipientId} AND is_read = 0 AND is_deleted = 0")
    Long countUnreadNotificationsByRecipientId(@Param("recipientId") Long recipientId);

    /**
     * 将单条通知标记为已读
     * @param notificationId 通知ID
     * @param recipientId 接收用户ID
     * @return 受影响的行数
     */
    @Update("UPDATE notification_recipient SET is_read = 1, read_at = CURRENT_TIMESTAMP WHERE notification_id = #{notificationId} AND recipient_id = #{recipientId}")
    int markAsRead(@Param("notificationId") Long notificationId, @Param("recipientId") Long recipientId);

    /**
     * 将用户所有未读通知标记为已读
     * @param recipientId 接收用户ID
     * @return 受影响的行数
     */
    @Update("UPDATE notification_recipient SET is_read = 1, read_at = CURRENT_TIMESTAMP WHERE recipient_id = #{recipientId} AND is_read = 0")
    int markAllAsRead(@Param("recipientId") Long recipientId);

    /**
     * 更新通知内容
     * @param notification 通知对象
     * @return 受影响的行数
     */
    @Update("UPDATE notification SET title = #{title}, content = #{content} WHERE id = #{id}")
    int updateNotification(Notification notification);

    /**
     * 根据UUID查找通知
     * @param uuid 通知的UUID
     * @return 通知对象
     */
    @Select("SELECT * FROM notification WHERE uuid = #{uuid}")
    Notification findNotificationByUuid(@Param("uuid") String uuid);

    /**
     * 软删除用户收到的某条通知
     * @param notificationId 通知ID
     * @param recipientId 接收用户ID
     * @return 受影响的行数
     */
    @Update("UPDATE notification_recipient SET is_deleted = 1 WHERE notification_id = #{notificationId} AND recipient_id = #{recipientId}")
    int softDelete(@Param("notificationId") Long notificationId, @Param("recipientId") Long recipientId);

    /**
     * [ADMIN] 分页查询所有通知记录
     * @param offset 分页偏移量
     * @param limit 每页数量
     * @return 通知列表
     */
    @Select("SELECT * FROM notification ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<Notification> findAllNotifications(@Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * [ADMIN] 统计所有通知总数
     * @return 通知总数
     */
    @Select("SELECT COUNT(*) FROM notification")
    Long countAllNotifications();
}
