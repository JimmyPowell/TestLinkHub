package tech.cspioneer.backend.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import tech.cspioneer.backend.service.NewsService;
import tech.cspioneer.backend.utils.JwtUtils;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;
import tech.cspioneer.backend.entity.dto.request.NewsUploadRequest;
import tech.cspioneer.backend.entity.dto.request.NewsUpdateRequest;
import tech.cspioneer.backend.entity.dto.request.NewsAuditReviewRequest;
import tech.cspioneer.backend.entity.dto.request.NewsQueryRequest;
import tech.cspioneer.backend.entity.dto.response.NewsListResponse;
import tech.cspioneer.backend.entity.dto.response.NewsDetailResponse;
import tech.cspioneer.backend.entity.dto.response.NewsAuditListResponse;
import tech.cspioneer.backend.entity.dto.response.NewsAuditDetailResponse;
import tech.cspioneer.backend.entity.dto.response.NewsHistoryResponse;
import java.util.List;
import org.springframework.security.web.AuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletResponse;

@WebMvcTest(AdminNewsController.class)
public class AdminNewsControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private NewsService newsService;
    @MockBean
    private JwtUtils jwtUtils;
    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable());
            http.exceptionHandling(e -> e.authenticationEntryPoint(testAuthenticationEntryPoint()));
            return http.build();
        }
        @Bean
        public AuthenticationEntryPoint testAuthenticationEntryPoint() {
            return (request, response, authException) -> {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
            };
        }
    }

    void setAuth(String uuid, String role) {
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(uuid, null, Collections.singleton(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("上传新闻-正常")
    void testUploadNews_Success() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        NewsUploadRequest req = new NewsUploadRequest();
        when(newsService.uploadNews(any())).thenReturn(1);
        mockMvc.perform(post("/api/admin/news/upload")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(() -> "admin-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("上传新闻-无权限")
    void testUploadNews_Forbidden() throws Exception {
        setAuth("user-uuid", "USER");
        NewsUploadRequest req = new NewsUploadRequest();
        when(newsService.uploadNews(any())).thenReturn(-1);
        mockMvc.perform(post("/api/admin/news/upload")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(() -> "user-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("上传新闻-服务异常")
    void testUploadNews_Exception() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        NewsUploadRequest req = new NewsUploadRequest();
        when(newsService.uploadNews(any())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(post("/api/admin/news/upload")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(() -> "admin-uuid"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("更新新闻-正常")
    void testUpdateNews_Success() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        NewsUpdateRequest req = new NewsUpdateRequest();
        when(newsService.updateNews(anyString(), any())).thenReturn(1);
        mockMvc.perform(put("/api/admin/news/update/uuid1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(() -> "admin-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("更新新闻-无权限")
    void testUpdateNews_Forbidden() throws Exception {
        setAuth("user-uuid", "USER");
        NewsUpdateRequest req = new NewsUpdateRequest();
        when(newsService.updateNews(anyString(), any())).thenReturn(-1);
        mockMvc.perform(put("/api/admin/news/update/uuid1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(() -> "user-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("更新新闻-服务异常")
    void testUpdateNews_Exception() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        NewsUpdateRequest req = new NewsUpdateRequest();
        when(newsService.updateNews(anyString(), any())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(put("/api/admin/news/update/uuid1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(() -> "admin-uuid"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("删除新闻-正常")
    void testDeleteNews_Success() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        when(newsService.deleteNews(anyString(), anyString(), anyString())).thenReturn(1);
        mockMvc.perform(put("/api/admin/news/delete/uuid1")
                .principal(() -> "admin-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("删除新闻-无权限")
    void testDeleteNews_Forbidden() throws Exception {
        setAuth("user-uuid", "USER");
        when(newsService.deleteNews(anyString(), anyString(), anyString())).thenReturn(-1);
        mockMvc.perform(put("/api/admin/news/delete/uuid1")
                .principal(() -> "user-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("删除新闻-服务异常")
    void testDeleteNews_Exception() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        when(newsService.deleteNews(anyString(), anyString(), anyString())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(put("/api/admin/news/delete/uuid1")
                .principal(() -> "admin-uuid"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("获取新闻详情-正常")
    void testGetNewsDetail_Success() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        when(newsService.getNewsDetailForAdmin(anyString())).thenReturn(new NewsDetailResponse());
        mockMvc.perform(get("/api/admin/news/detail/uuid1")
                .principal(() -> "admin-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("获取新闻列表-正常")
    void testGetAllNews_Success() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        when(newsService.getAllNews(any(), any(), anyString(), anyString())).thenReturn(List.of(new NewsListResponse()));
        mockMvc.perform(get("/api/admin/news/all")
                .principal(() -> "admin-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("新闻审核-正常")
    void testAuditNews_Success() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        NewsAuditReviewRequest req = new NewsAuditReviewRequest();
        mockMvc.perform(post("/api/root/news/auditNews/uuid1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(() -> "admin-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("新闻审核-服务异常")
    void testAuditNews_Exception() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        NewsAuditReviewRequest req = new NewsAuditReviewRequest();
        doThrow(new RuntimeException("error")).when(newsService).auditNews(anyString(), anyString(), any());
        mockMvc.perform(post("/api/root/news/auditNews/uuid1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(() -> "admin-uuid"))
                .andExpect(status().isInternalServerError());
    }

    // region root 删除新闻
    @Test
    @DisplayName("root删除新闻-正常")
    void testDeleteNewsAsRoot_Success() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        mockMvc.perform(delete("/api/root/news/uuid1")
                .principal(() -> "admin-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }
    @Test
    @DisplayName("root删除新闻-服务异常")
    void testDeleteNewsAsRoot_Exception() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        doThrow(new RuntimeException("error")).when(newsService).deleteNewsAsRoot(anyString());
        mockMvc.perform(delete("/api/root/news/uuid1")
                .principal(() -> "admin-uuid"))
                .andExpect(status().isInternalServerError());
    }
    @Test
    @DisplayName("root删除新闻-未认证")
    void testDeleteNewsAsRoot_Unauthenticated() throws Exception {
        SecurityContextHolder.clearContext();
        mockMvc.perform(delete("/api/root/news/uuid1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    // endregion

    // region 获取所有新闻
    @Test
    @DisplayName("获取所有新闻-分页参数为null")
    void testGetAllNews_NullPage() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        when(newsService.getAllNews(isNull(), isNull(), anyString(), anyString())).thenReturn(List.of());
        mockMvc.perform(get("/api/admin/news/all")
                .principal(() -> "admin-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
    @Test
    @DisplayName("获取所有新闻-分页参数为负数")
    void testGetAllNews_NegativePage() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        when(newsService.getAllNews(eq(-1), eq(-5), anyString(), anyString())).thenReturn(List.of());
        mockMvc.perform(get("/api/admin/news/all?page=-1&pageSize=-5")
                .principal(() -> "admin-uuid"))
                .andExpect(status().isOk());
    }
    @Test
    @DisplayName("获取所有新闻-身份COMPANY")
    void testGetAllNews_Company() throws Exception {
        setAuth("company-uuid", "COMPANY");
        when(newsService.getAllNews(any(), any(), anyString(), anyString())).thenReturn(List.of());
        mockMvc.perform(get("/api/admin/news/all")
                .principal(() -> "company-uuid"))
                .andExpect(status().isOk());
    }
    @Test
    @DisplayName("获取所有新闻-未知身份")
    void testGetAllNews_UnknownIdentity() throws Exception {
        setAuth("unknown-uuid", "UNKNOWN");
        when(newsService.getAllNews(any(), any(), anyString(), anyString())).thenReturn(List.of());
        mockMvc.perform(get("/api/admin/news/all")
                .principal(() -> "unknown-uuid"))
                .andExpect(status().isOk());
    }
    @Test
    @DisplayName("获取所有新闻-未认证")
    void testGetAllNews_Unauthenticated() throws Exception {
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/admin/news/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    @Test
    @DisplayName("获取所有新闻-服务异常")
    void testGetAllNews_Exception() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        when(newsService.getAllNews(any(), any(), anyString(), anyString())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(get("/api/admin/news/all")
                .principal(() -> "admin-uuid"))
                .andExpect(status().isInternalServerError());
    }
    // endregion

    // region 新闻列表 POST
    @Test
    @DisplayName("新闻列表-正常")
    void testGetNewsList_Success() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        NewsQueryRequest req = new NewsQueryRequest();
        when(newsService.getNewsList(any())).thenReturn(List.of(new NewsListResponse()));
        mockMvc.perform(post("/api/admin/news/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(() -> "admin-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
    @Test
    @DisplayName("新闻列表-分页参数为负数")
    void testGetNewsList_NegativePage() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        NewsQueryRequest req = new NewsQueryRequest();
        req.setPage(-1); req.setPageSize(-5);
        when(newsService.getNewsList(any())).thenReturn(List.of());
        mockMvc.perform(post("/api/admin/news/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(() -> "admin-uuid"))
                .andExpect(status().isOk());
    }
    @Test
    @DisplayName("新闻列表-身份COMPANY")
    void testGetNewsList_Company() throws Exception {
        setAuth("company-uuid", "COMPANY");
        NewsQueryRequest req = new NewsQueryRequest();
        when(newsService.getNewsList(any())).thenReturn(List.of());
        mockMvc.perform(post("/api/admin/news/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(() -> "company-uuid"))
                .andExpect(status().isOk());
    }
    @Test
    @DisplayName("新闻列表-未知身份")
    void testGetNewsList_UnknownIdentity() throws Exception {
        setAuth("unknown-uuid", "UNKNOWN");
        NewsQueryRequest req = new NewsQueryRequest();
        when(newsService.getNewsList(any())).thenReturn(List.of());
        mockMvc.perform(post("/api/admin/news/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(() -> "unknown-uuid"))
                .andExpect(status().isOk());
    }
    @Test
    @DisplayName("新闻列表-未认证")
    void testGetNewsList_Unauthenticated() throws Exception {
        SecurityContextHolder.clearContext();
        NewsQueryRequest req = new NewsQueryRequest();
        mockMvc.perform(post("/api/admin/news/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    @Test
    @DisplayName("新闻列表-服务异常")
    void testGetNewsList_Exception() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        NewsQueryRequest req = new NewsQueryRequest();
        when(newsService.getNewsList(any())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(post("/api/admin/news/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(() -> "admin-uuid"))
                .andExpect(status().isInternalServerError());
    }
    // endregion

    // region root 获取待审核新闻列表
    @Test
    @DisplayName("root获取待审核新闻-正常")
    void testGetNewsNeedToAudit_Success() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        when(newsService.getNewsAuditList(anyInt(), anyInt(), any(), anyString())).thenReturn(List.of(new NewsAuditListResponse()));
        mockMvc.perform(get("/api/root/news/audit")
                .principal(() -> "admin-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
    @Test
    @DisplayName("root获取待审核新闻-status为null")
    void testGetNewsNeedToAudit_NullStatus() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        when(newsService.getNewsAuditList(anyInt(), anyInt(), isNull(), anyString())).thenReturn(List.of());
        mockMvc.perform(get("/api/root/news/audit")
                .param("status", "")
                .principal(() -> "admin-uuid"))
                .andExpect(status().isOk());
    }
    @Test
    @DisplayName("root获取待审核新闻-未认证")
    void testGetNewsNeedToAudit_Unauthenticated() throws Exception {
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/root/news/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    @Test
    @DisplayName("root获取待审核新闻-服务异常")
    void testGetNewsNeedToAudit_Exception() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        when(newsService.getNewsAuditList(anyInt(), anyInt(), any(), anyString())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(get("/api/root/news/audit")
                .principal(() -> "admin-uuid"))
                .andExpect(status().isInternalServerError());
    }
    // endregion

    // region root 获取审核详情
    @Test
    @DisplayName("root获取审核详情-正常")
    void testGetNewsAuditDetail_Success() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        when(newsService.getNewsAuditDetail(anyString())).thenReturn(new NewsAuditDetailResponse());
        mockMvc.perform(get("/api/root/news/auditDetail/uuid1")
                .principal(() -> "admin-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }
    @Test
    @DisplayName("root获取审核详情-服务异常")
    void testGetNewsAuditDetail_Exception() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        when(newsService.getNewsAuditDetail(anyString())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(get("/api/root/news/auditDetail/uuid1")
                .principal(() -> "admin-uuid"))
                .andExpect(status().isInternalServerError());
    }
    @Test
    @DisplayName("root获取审核详情-未认证")
    void testGetNewsAuditDetail_Unauthenticated() throws Exception {
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/root/news/auditDetail/uuid1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    // endregion

    // region 获取新闻审核历史
    @Test
    @DisplayName("获取新闻审核历史-正常")
    void testGetNewsAuditHistoryList_Success() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        when(newsService.getNewsAuditHistoryList(anyString(), anyString(), anyString())).thenReturn(List.of(new NewsHistoryResponse()));
        mockMvc.perform(get("/api/admin/news/auditHistoryList/uuid1")
                .principal(() -> "admin-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
    @Test
    @DisplayName("获取新闻审核历史-身份COMPANY")
    void testGetNewsAuditHistoryList_Company() throws Exception {
        setAuth("company-uuid", "COMPANY");
        when(newsService.getNewsAuditHistoryList(anyString(), anyString(), anyString())).thenReturn(List.of());
        mockMvc.perform(get("/api/admin/news/auditHistoryList/uuid1")
                .principal(() -> "company-uuid"))
                .andExpect(status().isOk());
    }
    @Test
    @DisplayName("获取新闻审核历史-未知身份")
    void testGetNewsAuditHistoryList_UnknownIdentity() throws Exception {
        setAuth("unknown-uuid", "UNKNOWN");
        when(newsService.getNewsAuditHistoryList(anyString(), anyString(), anyString())).thenReturn(List.of());
        mockMvc.perform(get("/api/admin/news/auditHistoryList/uuid1")
                .principal(() -> "unknown-uuid"))
                .andExpect(status().isOk());
    }
    @Test
    @DisplayName("获取新闻审核历史-未认证")
    void testGetNewsAuditHistoryList_Unauthenticated() throws Exception {
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/admin/news/auditHistoryList/uuid1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    @Test
    @DisplayName("获取新闻审核历史-服务异常")
    void testGetNewsAuditHistoryList_Exception() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        when(newsService.getNewsAuditHistoryList(anyString(), anyString(), anyString())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(get("/api/admin/news/auditHistoryList/uuid1")
                .principal(() -> "admin-uuid"))
                .andExpect(status().isInternalServerError());
    }
    // endregion
} 