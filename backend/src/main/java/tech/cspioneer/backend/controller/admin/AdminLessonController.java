package tech.cspioneer.backend.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.exception.LessonServiceException;
import tech.cspioneer.backend.mapper.UserMapper;
import tech.cspioneer.backend.model.response.ApiResponse;
import tech.cspioneer.backend.service.LessonService;
import tech.cspioneer.backend.utils.JwtUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AdminLessonController {
    @Autowired
    private LessonService lessonService;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserMapper userMapper;

    @PostMapping("/admin/lesson/upload")
    public ResponseEntity<ApiResponse<Void>> uploadLesson(@RequestBody Map<String, Object> lessonRequestBody,
                                                          @AuthenticationPrincipal String userUuid,
                                                          Authentication authentication) {
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                String identity = authentication.getAuthorities().stream()
                        .findFirst()
                        .map(authority -> authority.getAuthority())
                        .orElse("UNKNOWN");
                lessonRequestBody.put("identity", identity);
            }
            lessonRequestBody.put("userId", userUuid);
            int result = lessonService.uploadLesson(lessonRequestBody);
            if (result == -1) {
                return ResponseEntity.ok(ApiResponse.error(403, "无权限"));
            }
            return ResponseEntity.ok(ApiResponse.success(200, "创建成功", null));
        } catch (LessonServiceException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(4001, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, "服务器内部错误"));
        }
    }

    @PutMapping("/admin/lesson/update")
    public ResponseEntity<?> updateLesson(@RequestParam(value = "uuid", required = false) String uuid,
                                          @RequestBody Map<String, Object> lessonRequestBody,
                                          @AuthenticationPrincipal String userUuid,
                                          Authentication authentication) {
        String identity = "UNKNOWN";
        if (authentication != null && authentication.isAuthenticated()) {
            identity = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(authority -> authority.getAuthority())
                    .orElse("UNKNOWN");
        }
        if (!("USER".equals(identity) || "COMPANY".equals(identity) || "ADMIN".equals(identity))) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "未知身份"));
        }
        if ("USER".equals(identity)) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, "权限不足"));
        }
        lessonRequestBody.put("identity", identity);
        lessonRequestBody.put("userId", userUuid);
        try {
            int result = lessonService.updateLesson(uuid, lessonRequestBody);
            if (result == -1) {
                return ResponseEntity.ok().body(ApiResponse.error(403, "无权限"));
            }
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", 200);
            resp.put("message", "修改成功");
            resp.put("data", null);
            return ResponseEntity.ok().body(resp);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, "服务器内部错误"));
        }
    }

    @DeleteMapping("/admin/lesson/delete")
    public ResponseEntity<?> deleteLesson(@RequestBody List<String> uuids,
                                          @AuthenticationPrincipal String userUuid,
                                          Authentication authentication) {
        String identity = "UNKNOWN";
        if (authentication != null && authentication.isAuthenticated()) {
            identity = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(authority -> authority.getAuthority())
                    .orElse("UNKNOWN");
        }
        if (!("USER".equals(identity) || "COMPANY".equals(identity) || "ADMIN".equals(identity))) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "未知身份"));
        }
        if ("USER".equals(identity) || "COMPANY".equals(identity)) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, "权限不足"));
        }
        try {
            int deleted = lessonService.deleteLesson(uuids);
            return ResponseEntity.ok().body(Map.of("code", 200, "message", "删除成功", "data", deleted));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, "服务器内部错误"));
        }
    }

    @GetMapping("/root/lesson/review/history")
    public ResponseEntity<?> getLessonReviewHistory(@RequestParam(value = "auditStatus", required = false) String auditStatus,
                                                   @RequestParam(value = "beginTime", required = false) String beginTime,
                                                   @RequestParam(value = "endTime", required = false) String endTime,
                                                   @RequestParam(value = "page", defaultValue = "0") int page,
                                                   @RequestParam(value = "size", defaultValue = "10") int size,
                                                   @AuthenticationPrincipal String userUuid,
                                                   Authentication authentication) {
        String identity = "UNKNOWN";
        if (authentication != null && authentication.isAuthenticated()) {
            identity = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(authority -> authority.getAuthority())
                    .orElse("UNKNOWN");
        }
        if (!"ADMIN".equals(identity)) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, "权限不足"));
        }
        Map<String, Object> data = lessonService.getLessonAuditHistoryPage(auditStatus, beginTime, endTime, page, size);
        return ResponseEntity.ok().body(Map.of("code", 200, "message", "历史查询成功", "data", data));
    }

    @PostMapping("/root/lesson/review/approval")
    public ResponseEntity<?> approveLesson(@RequestParam String uuid,
                                           @RequestBody Map<String, Object> approvalBody,
                                           @AuthenticationPrincipal String userUuid,
                                           Authentication authentication) {
        // 权限校验：只有ADMIN能访问
        String identity = "UNKNOWN";
        if (authentication != null && authentication.isAuthenticated()) {
            identity = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(authority -> authority.getAuthority())
                    .orElse("UNKNOWN");
        }
        if (!"ADMIN".equals(identity)) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, "权限不足"));
        }
        // 查user表主键id作为auditorId
        Long auditorId = null;
        if (userUuid != null) {
            User user = userMapper.findByUuid(userUuid);
            if (user != null) auditorId = user.getId();
        }
        approvalBody.put("auditorId", auditorId);
        int result = lessonService.approveLesson(uuid, approvalBody);
        if (result == 1) {
            return ResponseEntity.ok(ApiResponse.success(200, "审批操作成功", null));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "审批操作失败"));
        }
    }

    @GetMapping("/root/lesson/review/list")
    public ResponseEntity<?> getLessonReviewList(@RequestBody(required = false) Map<String, Object> pageBody,
                                                 @AuthenticationPrincipal String userUuid,
                                                 Authentication authentication) {
        String identity = "UNKNOWN";
        if (authentication != null && authentication.isAuthenticated()) {
            identity = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(authority -> authority.getAuthority())
                    .orElse("UNKNOWN");
        }
        if (!"ADMIN".equals(identity)) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, "权限不足"));
        }
        int page = 0, size = 10;
        if (pageBody != null) {
            if (pageBody.get("page") != null) page = (int) pageBody.get("page");
            if (pageBody.get("size") != null) size = (int) pageBody.get("size");
        }
        int offset = page * size;
        List<Map<String, Object>> list = lessonService.getReviewLessonsWithPendingVersion(size, offset);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 200);
        resp.put("message", "查询成功，待审核课程如下");
        resp.put("data", Map.of("total", list.size(), "list", list));
        return ResponseEntity.ok().body(resp);
    }

    @DeleteMapping("/root/lesson/review/history/delete")
    public ResponseEntity<?> deleteLessonReviewHistory(@RequestBody List<String> uuids,
                                                      @AuthenticationPrincipal String userUuid,
                                                      Authentication authentication) {
        String identity = "UNKNOWN";
        if (authentication != null && authentication.isAuthenticated()) {
            identity = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(authority -> authority.getAuthority())
                    .orElse("UNKNOWN");
        }
        if (!"ADMIN".equals(identity)) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, "权限不足"));
        }
        int deleted = lessonService.softDeleteLessonAuditHistory(uuids);
        return ResponseEntity.ok().body(Map.of("code", 200, "message", "删除成功", "data", deleted));
    }
}
