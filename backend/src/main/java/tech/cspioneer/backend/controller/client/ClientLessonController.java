package tech.cspioneer.backend.controller.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.entity.dto.request.LessonDetailRequest;
import tech.cspioneer.backend.entity.dto.request.LessonSearchRequest;
import tech.cspioneer.backend.entity.dto.response.LessonListResponse;
import tech.cspioneer.backend.entity.dto.response.LessonDetailResponse;
import tech.cspioneer.backend.entity.dto.response.LessonSearchResponse;
import tech.cspioneer.backend.exception.LessonServiceException;
import tech.cspioneer.backend.model.response.ApiResponse;
import tech.cspioneer.backend.service.LessonService;
import tech.cspioneer.backend.utils.JwtUtils;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ClientLessonController {
    @Autowired
    private LessonService lessonService;
    @Autowired
    private JwtUtils jwtUtils;

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
}
