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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.entity.dto.request.LessonUploadRequest;
import tech.cspioneer.backend.entity.dto.request.LessonUpdateRequest;
import tech.cspioneer.backend.entity.dto.request.LessonApprovalRequest;
import tech.cspioneer.backend.model.response.ApiResponse;
import tech.cspioneer.backend.service.LessonService;
import tech.cspioneer.backend.mapper.UserMapper;
import tech.cspioneer.backend.utils.JwtUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.BeforeEach;

@WebMvcTest(AdminLessonController.class)
public class AdminLessonControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private LessonService lessonService;
    @MockBean
    private UserMapper userMapper;
    @MockBean
    private JwtUtils jwtUtils;
    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable());
            return http.build();
        }
    }

    void setAuth(String uuid, String role) {
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(uuid, null, Collections.singleton(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("上传课程-正常")
    void testUploadLesson_Success() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        LessonUploadRequest req = new LessonUploadRequest();
        req.setName("test");
        when(lessonService.uploadLesson(any())).thenReturn(1);
        mockMvc.perform(post("/api/admin/lesson/upload")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(() -> "admin-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("上传课程-无权限")
    void testUploadLesson_Forbidden() throws Exception {
        setAuth("user-uuid", "USER");
        LessonUploadRequest req = new LessonUploadRequest();
        req.setName("test");
        when(lessonService.uploadLesson(any())).thenReturn(-1);
        mockMvc.perform(post("/api/admin/lesson/upload")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(() -> "user-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("上传课程-服务异常")
    void testUploadLesson_ServiceException() throws Exception {
        LessonUploadRequest req = new LessonUploadRequest();
        req.setName("test");
        when(lessonService.uploadLesson(any())).thenThrow(new tech.cspioneer.backend.exception.LessonServiceException("服务异常"));
        mockMvc.perform(post("/api/admin/lesson/upload")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(() -> "user-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4001));
    }

    @Test
    @DisplayName("上传课程-系统异常")
    void testUploadLesson_InternalError() throws Exception {
        LessonUploadRequest req = new LessonUploadRequest();
        req.setName("test");
        when(lessonService.uploadLesson(any())).thenThrow(new RuntimeException("系统异常"));
        mockMvc.perform(post("/api/admin/lesson/upload")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(() -> "user-uuid"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(5000));
    }

    @Test
    @DisplayName("更新课程-正常")
    void testUpdateLesson_Success() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        LessonUpdateRequest req = new LessonUpdateRequest();
        req.setName("update");
        when(lessonService.updateLesson(anyString(), any())).thenReturn(1);
        mockMvc.perform(put("/api/admin/lesson/update?uuid=lesson-uuid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(() -> "admin-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("修改成功"));
    }

    @Test
    @DisplayName("更新课程-未知身份")
    void testUpdateLesson_UnknownIdentity() throws Exception {
        setAuth("other-uuid", "OTHER");
        LessonUpdateRequest req = new LessonUpdateRequest();
        req.setName("update");
        mockMvc.perform(put("/api/admin/lesson/update?uuid=lesson-uuid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(() -> "other-uuid"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("更新课程-USER权限")
    void testUpdateLesson_UserForbidden() throws Exception {
        setAuth("user-uuid", "USER");
        LessonUpdateRequest req = new LessonUpdateRequest();
        req.setName("update");
        mockMvc.perform(put("/api/admin/lesson/update?uuid=lesson-uuid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(() -> "user-uuid"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("删除课程-正常")
    void testDeleteLesson_Success() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        when(lessonService.deleteLesson(anyList())).thenReturn(1);
        mockMvc.perform(delete("/api/admin/lesson/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of("uuid1","uuid2")))
                .principal(() -> "admin-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("删除成功"));
    }

    @Test
    @DisplayName("删除课程-未知身份")
    void testDeleteLesson_UnknownIdentity() throws Exception {
        setAuth("other-uuid", "OTHER");
        mockMvc.perform(delete("/api/admin/lesson/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of("uuid1")))
                .principal(() -> "other-uuid"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("课程审核总览-公司权限")
    void testOverviewLesson_Company() throws Exception {
        setAuth("company-uuid", "COMPANY");
        when(lessonService.getPendingReviewLessonsOverview(any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(new HashMap<>());
        mockMvc.perform(get("/api/admin/lesson/review/overview")
                .param("company_uuid", "company-uuid")
                .principal(() -> "company-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("课程审核总览-companyUuid为空")
    void testOverviewLesson_CompanyUuidNull() throws Exception {
        setAuth("company-uuid", "COMPANY");
        mockMvc.perform(get("/api/admin/lesson/review/overview")
                .param("company_uuid", "")
                .principal(() -> "company-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4001));
    }

    @Test
    @DisplayName("课程审核总览-权限不足")
    void testOverviewLesson_Forbidden() throws Exception {
        setAuth("user-uuid", "USER");
        mockMvc.perform(get("/api/admin/lesson/review/overview")
                .param("company_uuid", "company-uuid")
                .principal(() -> "user-uuid"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("课程审核历史-管理员权限")
    void testGetLessonReviewHistory_Admin() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        when(lessonService.getLessonAuditHistoryPage(any(), any(), any(), anyInt(), anyInt())).thenReturn(new HashMap<>());
        mockMvc.perform(get("/api/root/lesson/review/history")
                .param("page", "0").param("size", "10")
                .principal(() -> "admin-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("审批课程-正常")
    void testApproveLesson_Success() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        LessonApprovalRequest req = new LessonApprovalRequest();
        req.setAuditStatus("APPROVED");
        req.setComment("ok");
        when(lessonService.approveLesson(anyString(), any())).thenReturn(1);
        when(userMapper.findByUuid(anyString())).thenReturn(new User());
        mockMvc.perform(post("/api/root/lesson/review/approval")
                .param("uuid", "lesson-uuid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(() -> "admin-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("审批课程-审批失败")
    void testApproveLesson_Fail() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        LessonApprovalRequest req = new LessonApprovalRequest();
        req.setAuditStatus("REJECTED");
        req.setComment("不通过");
        when(lessonService.approveLesson(anyString(), any())).thenReturn(0);
        when(userMapper.findByUuid(anyString())).thenReturn(new User());
        mockMvc.perform(post("/api/root/lesson/review/approval")
                .param("uuid", "lesson-uuid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(() -> "admin-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("审批课程-无userUuid")
    void testApproveLesson_NoUserUuid() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        LessonApprovalRequest req = new LessonApprovalRequest();
        req.setAuditStatus("APPROVED");
        req.setComment("ok");
        when(lessonService.approveLesson(anyString(), any())).thenReturn(1);
        when(userMapper.findByUuid(anyString())).thenReturn(null);
        mockMvc.perform(post("/api/root/lesson/review/approval")
                .param("uuid", "lesson-uuid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(() -> null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取课程审核列表-正常")
    void testGetLessonReviewList_Success() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        when(lessonService.getReviewLessonsWithPendingVersion(anyInt(), anyInt(), any(), any(), any()))
            .thenReturn(java.util.List.of(new java.util.HashMap<>()));
        mockMvc.perform(get("/api/root/lesson/review/list")
                .param("size", "10").param("page", "0")
                .principal(() -> "admin-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("删除课程审核历史-正常")
    void testDeleteLessonReviewHistory_Success() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        when(lessonService.softDeleteLessonAuditHistory(anyList())).thenReturn(1);
        mockMvc.perform(delete("/api/root/lesson/review/history/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of("uuid1","uuid2")))
                .principal(() -> "admin-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("删除成功"));
    }

    @Test
    @DisplayName("获取课程详情-正常")
    void testGetLessonDetailForRoot_Success() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        when(lessonService.getLessonDetailForRoot(anyString())).thenReturn(new HashMap<>());
        mockMvc.perform(get("/api/root/lesson/detail/lesson-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取课程详情-业务异常")
    void testGetLessonDetailForRoot_BizException() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        when(lessonService.getLessonDetailForRoot(anyString())).thenThrow(new tech.cspioneer.backend.exception.LessonServiceException("not found"));
        mockMvc.perform(get("/api/root/lesson/detail/lesson-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("获取课程详情-系统异常")
    void testGetLessonDetailForRoot_InternalError() throws Exception {
        setAuth("admin-uuid", "ADMIN");
        when(lessonService.getLessonDetailForRoot(anyString())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(get("/api/root/lesson/detail/lesson-uuid"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(5000));
    }

    @Test
    @DisplayName("上传课程-未认证")
    void testUploadLesson_NoAuth() throws Exception {
        SecurityContextHolder.clearContext();
        LessonUploadRequest req = new LessonUploadRequest();
        req.setName("test");
        when(lessonService.uploadLesson(any())).thenReturn(1);
        mockMvc.perform(post("/api/admin/lesson/upload")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("公司课程查询-公司权限")
    void testGetCompanyLessons_Company() throws Exception {
        setAuth("company-uuid", "COMPANY");
        when(lessonService.getLessonsByCompany(anyString(), any(), any(), any(), anyInt(), anyInt())).thenReturn(new tech.cspioneer.backend.entity.dto.response.LessonListResponse());
        mockMvc.perform(get("/api/admin/lesson/company")
                .param("uuid", "lesson-uuid")
                .param("name", "test")
                .param("status", "PENDING")
                .param("page", "0")
                .param("size", "10")
                .principal(() -> "company-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // TODO: 依次补全所有接口的正常分支、异常分支、参数校验等测试用例
} 