package tech.cspioneer.backend.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.entity.Notification;
import tech.cspioneer.backend.entity.dto.request.AnnouncementRequest;
import tech.cspioneer.backend.model.response.ApiResponse;
import tech.cspioneer.backend.service.NotificationService;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final NotificationService notificationService;

    /**
     * [ADMIN] 分页获取系统中的所有通知
     * @param page 页码
     * @param size 每页数量
     * @return 分页的通知数据
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Notification>>> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Notification> notificationPage = notificationService.getAllNotifications(page, size);
        return ResponseEntity.ok(ApiResponse.success(200, "All notifications retrieved successfully.", notificationPage));
    }

    /**
     * [ADMIN] 发送系统公告
     * @param request 公告请求体
     * @return 操作结果
     */
    @PostMapping("/announcements")
    public ResponseEntity<ApiResponse<Void>> sendAnnouncement(@RequestBody AnnouncementRequest request) {
        // Here, we can expand logic based on targetAudience
        if ("ALL".equalsIgnoreCase(request.getTargetAudience())) {
            notificationService.sendSystemNotificationToAll(request.getTitle(), request.getContent(), null, null);
        } else if ("COMPANY".equalsIgnoreCase(request.getTargetAudience()) && request.getCompanyId() != null) {
            notificationService.sendSystemNotificationToCompany(request.getCompanyId(), request.getTitle(), request.getContent(), null, null);
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Invalid target audience."));
        }
        
        return ResponseEntity.ok(ApiResponse.success(202, "Announcement distribution started.", null));
    }
}
