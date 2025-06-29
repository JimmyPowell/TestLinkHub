package tech.cspioneer.backend.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.entity.dto.request.*;
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
    public ResponseEntity<ApiResponse<Void>> uploadLesson(@RequestBody LessonUploadRequest lessonUploadRequest,
                                                          @AuthenticationPrincipal String userUuid,
                                                          Authentication authentication) {
        try {
            Map<String, Object> lessonRequestBody = new HashMap<>();
            lessonRequestBody.put("name", lessonUploadRequest.getName());
            lessonRequestBody.put("imageUrl", lessonUploadRequest.getImageUrl());
            lessonRequestBody.put("description", lessonUploadRequest.getDescription());
            lessonRequestBody.put("authorName", lessonUploadRequest.getAuthorName());
            lessonRequestBody.put("resourcesType", lessonUploadRequest.getResourcesType());
            lessonRequestBody.put("resourcesUrls", lessonUploadRequest.getResourcesUrls());
            lessonRequestBody.put("resourceNames", lessonUploadRequest.getResourceNames());
            lessonRequestBody.put("sortOrders", lessonUploadRequest.getSortOrders());
            
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
                                          @RequestBody LessonUpdateRequest lessonUpdateRequest,
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
        
        Map<String, Object> lessonRequestBody = new HashMap<>();
        lessonRequestBody.put("name", lessonUpdateRequest.getName());
        lessonRequestBody.put("imageUrl", lessonUpdateRequest.getImageUrl());
        lessonRequestBody.put("description", lessonUpdateRequest.getDescription());
        lessonRequestBody.put("authorName", lessonUpdateRequest.getAuthorName());
        lessonRequestBody.put("resourcesType", lessonUpdateRequest.getResourcesType());
        lessonRequestBody.put("resourcesUrls", lessonUpdateRequest.getResourcesUrls());
        lessonRequestBody.put("resourceNames", lessonUpdateRequest.getResourceNames());
        lessonRequestBody.put("sortOrders", lessonUpdateRequest.getSortOrders());
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

        System.out.println("--- 进入删除课程端点 ---");
        System.out.println("请求删除的课程UUIDs: " + uuids);
        System.out.println("操作用户的UUID: " + userUuid);

        String identity = "UNKNOWN";
        if (authentication != null && authentication.isAuthenticated()) {
            identity = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(authority -> authority.getAuthority())
                    .orElse("UNKNOWN");
            System.out.println("解析出的用户身份: " + identity);
        } else {
            System.out.println("无法获取认证信息");
        }

        if (!("USER".equals(identity) || "COMPANY".equals(identity) || "ADMIN".equals(identity))) {
            System.out.println("权限检查：未知身份，拒绝访问");
            return ResponseEntity.status(401).body(ApiResponse.error(401, "未知身份"));
        }

        // 这里的逻辑需要调整，暂时只允许ADMIN删除
        // if ("USER".equals(identity) || "COMPANY".equals(identity)) {
        //     System.out.println("权限检查：用户身份为 " + identity + "，权限不足，拒绝访问");
        //     return ResponseEntity.status(403).body(ApiResponse.error(403, "权限不足"));
        // }
        // 应该在Service层检查课程所有权，而不是在这里简单地按角色拒绝

        try {
            System.out.println("权限检查通过，准备调用服务层删除课程...");
            int deleted = lessonService.deleteLesson(uuids);
            return ResponseEntity.ok().body(Map.of("code", 200, "message", "删除成功", "data", deleted));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, "服务器内部错误"));
        }
    }

    @GetMapping("/admin/lesson/review/overview")
    public ResponseEntity<?> overviewLesson(
        @RequestParam(value = "name", required = false) String name,
        @RequestParam(value = "status", required = false) String status,
        @RequestParam(value = "lesson_uuid", required = false) String lessonUuid,
        @RequestParam(value = "company_uuid") String companyUuid,
        @RequestParam(defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "10") Integer size,
        Authentication authentication
    ) {
        String identity = "UNKNOWN";
        if (authentication != null && authentication.isAuthenticated()) {
            identity = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(authority -> authority.getAuthority())
                    .orElse("UNKNOWN");
        }
        if (!("COMPANY".equals(identity) || "ADMIN".equals(identity))) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, "权限不足"));
        }
        if (companyUuid == null || companyUuid.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(4001, "company_uuid不能为空"));
        }
        try {
            Map<String, Object> result = lessonService.getPendingReviewLessonsOverview(
                name, status, lessonUuid, companyUuid, page, size
            );
            return ResponseEntity.ok().body(Map.of("code", 200, "message", "查询成功", "data", result));
        } catch (LessonServiceException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(4001, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, "服务器内部错误"));
        }
    }

    @GetMapping("/root/lesson/review/history")
    public ResponseEntity<?> getLessonReviewHistory(@RequestParam(value = "audit_status", required = false) String auditStatus,
                                                   @RequestParam(value = "begin_time", required = false) String beginTime,
                                                   @RequestParam(value = "end_time", required = false) String endTime,
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
                                           @RequestBody LessonApprovalRequest approvalRequest,
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
        
        Map<String, Object> approvalBody = new HashMap<>();
        approvalBody.put("auditStatus", approvalRequest.getAuditStatus());
        approvalBody.put("comment", approvalRequest.getComment());
        approvalBody.put("auditorId", auditorId);
        
        int result = lessonService.approveLesson(uuid, approvalBody);
        if (result == 1) {
            return ResponseEntity.ok(ApiResponse.success(200, "审批操作成功", null));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "审批操作失败"));
        }
    }

    @GetMapping("/root/lesson/review/list")
    public ResponseEntity<?> getLessonReviewList(@RequestParam(defaultValue = "0") Integer size,
                                                 @RequestParam(defaultValue = "10") Integer page,
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

    @GetMapping("/admin/lesson/company")
    public ResponseEntity<?> getCompanyLessons(
            @AuthenticationPrincipal String userUuid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        String identity = "UNKNOWN";
        if (authentication != null && authentication.isAuthenticated()) {
            identity = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(authority -> authority.getAuthority())
                    .orElse("UNKNOWN");
        }

        if (!"COMPANY".equals(identity)) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, "此端点仅供公司管理员使用"));
        }

        try {
            var result = lessonService.getLessonsByCompany(userUuid, page, size);
            return ResponseEntity.ok().body(Map.of("code", 200, "message", "查询成功", "data", result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, "服务器内部错误"));
        }
    }
}
