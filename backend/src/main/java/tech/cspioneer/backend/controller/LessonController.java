package tech.cspioneer.backend.controller;

import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tech.cspioneer.backend.service.LessonService;
import tech.cspioneer.backend.utils.JwtUtils;
import tech.cspioneer.backend.model.response.ApiResponse;
import tech.cspioneer.backend.exception.LessonServiceException;
import tech.cspioneer.backend.entity.dto.response.LessonListResponse;
import tech.cspioneer.backend.entity.dto.request.LessonDetailRequest;
import tech.cspioneer.backend.entity.dto.response.LessonDetailResponse;
import tech.cspioneer.backend.entity.dto.request.LessonSearchRequest;
import tech.cspioneer.backend.entity.dto.response.LessonSearchResponse;

@RestController
@RequestMapping("/api")
public class LessonController {

    @Autowired
    private LessonService lessonService;
    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 管理员/企业用户上传课程接口
     * 1. 从token解析用户身份
     * 2. 传递userUuid和identity到service
     * 3. 由service层分流处理
     */
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
            //
            int result = lessonService.uploadLesson(lessonRequestBody);
            if (result == -1) {
                // 权限不足，返回code=403
                return ResponseEntity.ok(ApiResponse.error(403, "无权限"));
            }
            return ResponseEntity.ok(ApiResponse.success(200, "创建成功", null));
        } catch (LessonServiceException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(4001, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, "服务器内部错误"));
        }
    }

    // 2. 课程修改
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
        // COMPANY和ADMIN可以访问
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

    // 3. 课程删除
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

    // 4. 课程浏览（用户端）
    @GetMapping("/user/lesson/all")
    public ResponseEntity<?> getAllLessons(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String authorName,
            @RequestParam(required = false) String beginTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
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
        try {
            LessonListResponse result = lessonService.getAllLessons(name, authorName, beginTime, endTime, page, size);
            return ResponseEntity.ok().body(Map.of("code", 200, "message", "查询成功，符合条件课程如下", "data", result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, "服务器内部错误"));
        }
    }

    // 5. 课程详情
    @GetMapping("/user/lesson/detail")
    public ResponseEntity<?> getLessonDetail(@RequestBody LessonDetailRequest req,
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
        try {
            LessonDetailResponse result = lessonService.getLessonDetail(req);
            return ResponseEntity.ok().body(Map.of("code", 200, "message", "查询成功，详细信息如下", "data", result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, "服务器内部错误"));
        }
    }

    // 6. 课程搜索
    @GetMapping("/user/lesson/search")
    public ResponseEntity<?> searchLesson(@RequestBody LessonSearchRequest req,
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
        try {
            LessonSearchResponse result = lessonService.searchLesson(req);
            return ResponseEntity.ok().body(Map.of("code", 200, "message", "查询成功，课程搜索如下", "data", result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, "服务器内部错误"));
        }
    }

    // 7. 审核列表（超级管理员）
    @PostMapping("/root/lesson/review/history")
    public ResponseEntity<?> getLessonReviewHistory(@RequestBody(required = false) Map<String, Object> pageBody) {
        // TODO: 调用service实现审核列表
        return ResponseEntity.ok().body(Map.of("code", 200, "message", "查询成功，待审核课程如下", "data", Map.of("total", 0, "list", List.of())));
    }

    // 8. 审核操作（超级管理员）
    @PostMapping("/root/lesson/review/approval")
    public ResponseEntity<?> approveLesson(@RequestParam(value = "uuid", required = false) String uuid,
                                           @RequestBody Map<String, Object> approvalBody) {
        // TODO: 调用service实现课程审核
        return ResponseEntity.ok().body(Map.of("code", 200, "message", "审批成功", "data", null));
    }

    // 9. 审核历史（超级管理员）
    @GetMapping("/root/lesson/review/list")
    public ResponseEntity<?> getLessonReviewList(@RequestBody(required = false) Map<String, Object> pageBody) {
        // TODO: 调用service实现审核历史
        return ResponseEntity.ok().body(Map.of("code", 200, "message", "查询成功，审核历史如下", "data", Map.of("total", 0, "list", List.of())));
    }

    // 10. 删除审核历史（超级管理员）
    @DeleteMapping("/root/lesson/review/history/delete")
    public ResponseEntity<?> deleteLessonReviewHistory(@RequestParam(value = "uuids", required = false) List<String> uuids) {
        // TODO: 调用service实现删除审核历史
        return ResponseEntity.ok().body(Map.of("code", 200, "message", "删除成功", "data", null));
    }
}