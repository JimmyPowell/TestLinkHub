package tech.cspioneer.backend.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import tech.cspioneer.backend.service.LessonService;
import tech.cspioneer.backend.utils.JwtUtils;
import tech.cspioneer.backend.model.response.ApiResponse;
import tech.cspioneer.backend.exception.LessonServiceException;

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
                                          HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Map<String, Object> payload = jwtUtils.decode(token);
            String userUuid = (String) payload.get("uid");
            String identity = (String) payload.get("identity");
            System.out.println("身份"+identity);
            lessonRequestBody.put("userId", userUuid);
            lessonRequestBody.put("identity", identity);
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
                                          @RequestBody Map<String, Object> lessonRequestBody) {
        // TODO: 调用service实现课程修改
        return ResponseEntity.ok().body(Map.of("code", 200, "message", "修改成功", "data", null));
    }

    // 3. 课程删除
    @DeleteMapping("/admin/lesson/delete")
    public ResponseEntity<?> deleteLesson(@RequestParam(value = "uuids", required = false) List<String> uuids) {
        // TODO: 调用service实现课程删除
        return ResponseEntity.ok().body(Map.of("code", 200, "message", "删除成功", "data", null));
    }

    // 4. 课程搜索（用户端）
    @GetMapping("/user/lesson/find")
    public ResponseEntity<?> findLesson(@RequestBody(required = false) Map<String, Object> searchBody) {
        // TODO: 调用service实现课程搜索
        return ResponseEntity.ok().body(Map.of("code", 200, "message", "查询成功，课程列表如下", "data", Map.of("total", 0, "list", List.of())));
    }

    // 5. 课程浏览（用户端）
    @GetMapping("/user/lesson/all")
    public ResponseEntity<?> getAllLessons(@RequestBody(required = false) Map<String, Object> pageBody) {
        // TODO: 调用service实现课程浏览
        return ResponseEntity.ok().body(Map.of("code", 200, "message", "查询成功，符合条件课程如下", "data", Map.of("total", 0, "list", List.of())));
    }

    // 6. 课程详情
    @GetMapping("/user/lesson/detail")
    public ResponseEntity<?> getLessonDetail(@RequestParam(value = "uuid", required = false) String uuid) {
        // TODO: 调用service实现课程详情
        return ResponseEntity.ok().body(Map.of("code", 200, "message", "查询成功，详细信息如下", "data", Map.of()));
    }

    // 7. 审核列表（超级管理员）
    @GetMapping("/root/lesson/review/history")
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