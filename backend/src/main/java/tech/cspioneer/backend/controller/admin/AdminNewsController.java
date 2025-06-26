package tech.cspioneer.backend.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.entity.dto.request.NewsAuditReviewRequest;
import tech.cspioneer.backend.entity.dto.request.NewsUploadRequest;
import tech.cspioneer.backend.entity.dto.request.NewsUpdateRequest;
import tech.cspioneer.backend.entity.dto.response.NewsAuditDetailResponse;
import tech.cspioneer.backend.entity.dto.response.NewsAuditListResponse;
import tech.cspioneer.backend.entity.dto.response.NewsHistoryResponse;
import tech.cspioneer.backend.entity.dto.response.NewsListResponse;
import tech.cspioneer.backend.exception.LessonServiceException;
import tech.cspioneer.backend.exception.NewsServiceException;
import tech.cspioneer.backend.model.response.ApiResponse;
import tech.cspioneer.backend.service.NewsService;
import tech.cspioneer.backend.utils.JwtUtils;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminNewsController{
    @Autowired
    private NewsService newsService;
    @Autowired
    private JwtUtils jwtUtils;
    Logger logger = LoggerFactory.getLogger(AdminNewsController.class);
    @PostMapping("/admin/news/upload")
    @PreAuthorize("hasAnyAuthority('COMPANY','ADMIN')")
    public ResponseEntity<ApiResponse<String>> uploadNews(
            @RequestBody NewsUploadRequest newsUploadRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userUuid = (String) authentication.getPrincipal();
        // 获取用户身份/角色
        String identity = authentication.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse("UNKNOWN");
        newsUploadRequest.setUserUUid(userUuid);
        newsUploadRequest.setIdentity(identity);
        int result = newsService.uploadNews(newsUploadRequest);
        if (result == -1) {
            return ResponseEntity.ok(ApiResponse.error(403, "无权限"));
        }
        return ResponseEntity.ok(ApiResponse.success(200, "新闻创建成功", null));
    }

    @PutMapping("/admin/news/update/{uuid}")
    @PreAuthorize("hasAnyAuthority('COMPANY','ADMIN')")
    public ResponseEntity<ApiResponse<String>> updateNews(
            @PathVariable String uuid,
            @RequestBody NewsUpdateRequest newsUpdateRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userUuid = (String) authentication.getPrincipal();
        // 获取用户身份/角色
        String identity = authentication.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse("UNKNOWN");
        newsUpdateRequest.setUserUUid(userUuid);
        newsUpdateRequest.setIdentity(identity);
        int result = newsService.updateNews(uuid, newsUpdateRequest);
        if (result == -1) {
            // 权限不足，返回code=403
            return ResponseEntity.ok(ApiResponse.error(403, "无权限"));
        }
        return ResponseEntity.ok(ApiResponse.success(200, "新闻更新成功", null));
    }

    @PutMapping("/admin/news/delete/{uuid}")
    @PreAuthorize("hasAnyAuthority('COMPANY','ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteNews(
            @PathVariable String uuid) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userUuid = (String) authentication.getPrincipal();
        // 获取用户身份/角色
        String identity = authentication.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse("UNKNOWN");
        int result = newsService.deleteNews(uuid,userUuid,identity);
        if (result == -1) {
            // 权限不足，返回code=403
            return ResponseEntity.ok(ApiResponse.error(403, "无权限"));
        }
        return ResponseEntity.ok(ApiResponse.success("新闻删除成功"));
    }

    @GetMapping("/admin/news/all")
    @PreAuthorize("hasAnyAuthority('COMPANY','ADMIN')")
    public ResponseEntity<ApiResponse<?>> getAllNews(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userUuid = (String) authentication.getPrincipal();
        // 获取用户身份/角色
        String identity = authentication.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse("UNKNOWN");
        List<NewsListResponse> newsListResponses = newsService.getAllNews(page, pageSize, userUuid, identity);
        return ResponseEntity.ok(ApiResponse.success(newsListResponses));
    }

    @GetMapping("/root/news/audit")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<?>> getNewsNeedToAudit(
            @RequestParam(required = false) int page,
            @RequestParam(required = false) int pageSize,
            @AuthenticationPrincipal String adminUuid) {
        List<NewsAuditListResponse> newsAuditList = newsService.getNewsAuditList(page,pageSize,adminUuid);
        return ResponseEntity.ok(ApiResponse.success(newsAuditList));
    }

    @GetMapping("/root/news/auditDetail/{uuid}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<?>> getNewsAuditDetail(@PathVariable String uuid) {
        NewsAuditDetailResponse newsAuditDetailResponse = newsService.getNewsAuditDetail(uuid);
        return ResponseEntity.ok(ApiResponse.success(newsAuditDetailResponse));
    }

    @PostMapping("/root/news/auditNews/{uuid}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<String>> auditNews(
            @PathVariable String uuid,
            @AuthenticationPrincipal String adminUuid,
            @RequestBody NewsAuditReviewRequest newsAuditReviewRequest) {
        newsService.auditNews(uuid,adminUuid,newsAuditReviewRequest);
        return ResponseEntity.ok(ApiResponse.success("审核完毕"));
    }

    @GetMapping("admin/news/auditHistoryList/{uuid}")
    @PreAuthorize("hasAnyAuthority('COMPANY','ADMIN')")
    public ResponseEntity<ApiResponse<?>> getNewsAuditHistoryList(@PathVariable String uuid) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userUuid = (String) authentication.getPrincipal();
        // 获取用户身份/角色
        String identity = authentication.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse("UNKNOWN");
        List<NewsHistoryResponse> newsAuditListResponses = newsService.getNewsAuditHistoryList(uuid,userUuid,identity);
        return ResponseEntity.ok(ApiResponse.success(newsAuditListResponses));
    }
}
