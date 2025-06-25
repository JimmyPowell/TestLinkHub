package tech.cspioneer.backend.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.entity.dto.request.NewsUploadRequest;
import tech.cspioneer.backend.entity.dto.request.NewsUpdateRequest;
import tech.cspioneer.backend.exception.LessonServiceException;
import tech.cspioneer.backend.exception.NewsServiceException;
import tech.cspioneer.backend.model.response.ApiResponse;
import tech.cspioneer.backend.service.NewsService;
import tech.cspioneer.backend.utils.JwtUtils;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/news")
@RequiredArgsConstructor
public class AdminNewsController{
    @Autowired
    private NewsService newsService;
    @Autowired
    private JwtUtils jwtUtils;
    Logger logger = LoggerFactory.getLogger(AdminNewsController.class);
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadNews(
            @RequestBody NewsUploadRequest newsUploadRequest,
            HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Map<String, Object> payload = jwtUtils.decode(token);
            String userUuid = (String) payload.get("uid");
            String identity = (String) payload.get("identity");
            logger.info("身份"+identity);
            newsUploadRequest.setUserUUid(userUuid);
            newsUploadRequest.setIdentity(identity);
            int result = newsService.uploadNews(newsUploadRequest);
            if (result == -1) {
                // 权限不足，返回code=403
                return ResponseEntity.ok(ApiResponse.error(403, "无权限"));
            }
            return ResponseEntity.ok(ApiResponse.success(200, "新闻创建成功", null));
        } catch (NewsServiceException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(4001, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, "服务器内部错误"));
        }
    }

    @PutMapping("/update/{uuid}")
    public ResponseEntity<ApiResponse<String>> updateNews(
            @PathVariable String uuid,
            @RequestBody NewsUpdateRequest newsUpdateRequest,
            HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Map<String, Object> payload = jwtUtils.decode(token);
            String userUuid = (String) payload.get("uid");
            String identity = (String) payload.get("identity");
            logger.info("身份"+identity);
            newsUpdateRequest.setUserUUid(userUuid);
            newsUpdateRequest.setIdentity(identity);
            int result = newsService.updateNews(uuid, newsUpdateRequest);
            if (result == -1) {
                // 权限不足，返回code=403
                return ResponseEntity.ok(ApiResponse.error(403, "无权限"));
            }
            return ResponseEntity.ok(ApiResponse.success(200, "新闻更新成功", null));
        } catch (NewsServiceException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(4001, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, "服务器内部错误"));
        }
    }

    @PutMapping("/delete/{uuid}")
    public ResponseEntity<ApiResponse<String>> deleteNews(
            @PathVariable String uuid) {
        newsService.deleteNews(uuid);
        return ResponseEntity.ok(ApiResponse.success("新闻删除成功"));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<?>> getAllNews(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        return ResponseEntity.ok(
            ApiResponse.success(newsService.getAllNews(page, pageSize)));
    }
}
