package tech.cspioneer.backend.controller.client;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.entity.dto.request.MarkNotificationsAsReadRequest;
import tech.cspioneer.backend.entity.dto.response.NotificationResponse;
import tech.cspioneer.backend.exception.ResourceNotFoundException;
import tech.cspioneer.backend.model.response.ApiResponse;
import tech.cspioneer.backend.service.NotificationService;

@RestController
@RequestMapping("/api/client/notifications")
@RequiredArgsConstructor



public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 分页获取当前用户的通知列表
     * @param userDetails Spring Security 用户详情
     * @param status 状态 (all, read, unread), 默认为 "all"
     * @param page 页码, 默认为 0
     * @param size 每页数量, 默认为 10
     * @return 分页的通知数据
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        String userUuid = userDetails.getUsername(); // JWT subject is user's UUID
        Page<NotificationResponse> notificationPage = notificationService.getNotificationsByUserUuid(userUuid, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(200, "Notifications retrieved successfully.", notificationPage));
    }

    /**
     * 获取单条通知详情
     * @param userDetails Spring Security 用户详情
     * @param uuid 通知的UUID
     * @return 通知详情
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotificationDetails(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String uuid) {
        try {
            String userUuid = userDetails.getUsername();
            NotificationResponse notification = notificationService.getNotificationDetails(uuid, userUuid);
            return ResponseEntity.ok(ApiResponse.success(200, "Notification details retrieved successfully.", notification));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, e.getMessage()));
        }
    }

    /**
     * 将单条通知标记为已读
     * @param userDetails Spring Security 用户详情
     * @param uuid 通知的UUID
     * @return 操作结果
     */
    @PatchMapping("/{uuid}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String uuid) {
        try {
            String userUuid = userDetails.getUsername();
            notificationService.markNotificationAsRead(uuid, userUuid);
            return ResponseEntity.ok(ApiResponse.success(200, "Notification marked as read.", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, e.getMessage()));
        }
    }

    /**
     * 批量标记通知为已读
     * @param userDetails Spring Security 用户详情
     * @param request 包含通知UUID列表的请求体
     * @return 操作结果
     */
    @PatchMapping("/read")
    public ResponseEntity<ApiResponse<Void>> markBatchAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody MarkNotificationsAsReadRequest request) {
        try {
            String userUuid = userDetails.getUsername();
            notificationService.markNotificationsAsRead(request.getNotificationUuids(), userUuid);
            return ResponseEntity.ok(ApiResponse.success(200, "Notifications marked as read.", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, e.getMessage()));
        }
    }

    /**
     * 标记所有通知为已读
     * @param userDetails Spring Security 用户详情
     * @return 操作结果
     */
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        String userUuid = userDetails.getUsername();
        notificationService.markAllNotificationsAsRead(userUuid);
        return ResponseEntity.ok(ApiResponse.success(200, "All notifications marked as read.", null));
    }

    /**
     * 删除单条通知
     * @param userDetails Spring Security 用户详情
     * @param uuid 通知的UUID
     * @return 操作结果
     */


    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String uuid) {
        try {
            String userUuid = userDetails.getUsername();
            notificationService.deleteNotification(uuid, userUuid);
            return ResponseEntity.ok(ApiResponse.success(204, "Notification deleted successfully.", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, e.getMessage()));
        }
    }
}
