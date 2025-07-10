package tech.cspioneer.backend.controller.client;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.entity.dto.request.NewsQueryRequest;
import tech.cspioneer.backend.entity.dto.response.NewsDetailResponse;
import tech.cspioneer.backend.entity.dto.response.NewsListResponse;
import tech.cspioneer.backend.model.response.ApiResponse;
import tech.cspioneer.backend.service.NewsService;

import java.util.List;

@RestController
@RequestMapping("/api/user/news")
@RequiredArgsConstructor
public class ClientNewsController{
    @Autowired
    private NewsService newsService;

    @GetMapping("/newsList")
    @PreAuthorize("hasAnyAuthority('USER','COMPANY','ADMIN')")
    public ResponseEntity<ApiResponse<List<NewsListResponse>>> getNewsList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "16") Integer pageSize,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String summary,
            @RequestParam(required = false) String title) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userUuid = (String) authentication.getPrincipal();
        // 获取用户身份/角色
        String identity = authentication.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse("UNKNOWN");
        NewsQueryRequest newsQueryRequest = new NewsQueryRequest();
        newsQueryRequest.setTitle(title);
        newsQueryRequest.setSummary(summary);
        newsQueryRequest.setEndTime(endTime);
        newsQueryRequest.setStartTime(startTime);
        newsQueryRequest.setPage(page);
        newsQueryRequest.setPageSize(pageSize);
        newsQueryRequest.setIdentity(identity);
        newsQueryRequest.setUserUuid(userUuid);
        List<NewsListResponse> response = newsService.getNewsList(newsQueryRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/getNews/{uuid}")
    @PreAuthorize("hasAnyAuthority('USER','COMPANY','ADMIN')")
    public ResponseEntity<ApiResponse<NewsDetailResponse>> getNewsDetail(
            @PathVariable String uuid) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userUuid = (String) authentication.getPrincipal();
        // 获取用户身份/角色
        String identity = authentication.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse("UNKNOWN");
        NewsDetailResponse response = newsService.getNewsDetail(uuid,userUuid,identity);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
