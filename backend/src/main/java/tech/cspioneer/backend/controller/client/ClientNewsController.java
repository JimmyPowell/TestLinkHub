package tech.cspioneer.backend.controller.client;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.entity.dto.request.NewsQueryRequest;
import tech.cspioneer.backend.entity.dto.response.NewsDetailResponse;
import tech.cspioneer.backend.entity.dto.response.NewsListResponse;
import tech.cspioneer.backend.model.response.ApiResponse;
import tech.cspioneer.backend.service.NewsService;

import java.util.List;

@RestController
@RequestMapping("/api/client/news") 
@RequiredArgsConstructor
public class ClientNewsController{
    @Autowired
    private NewsService newsService;

    @GetMapping("/newsList")
    public ResponseEntity<ApiResponse<List<NewsListResponse>>> getNewsList(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestBody NewsQueryRequest newsQueryRequest,
            @AuthenticationPrincipal String userUuid) {
        if (userUuid == null) {

        }
        List<NewsListResponse> response = newsService.getNewsList(page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/getNews/{uuid}")
    public ResponseEntity<ApiResponse<NewsDetailResponse>> getNewsDetail(
            @PathVariable String uuid) {
        NewsDetailResponse response = newsService.getNewsDetail(uuid);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
