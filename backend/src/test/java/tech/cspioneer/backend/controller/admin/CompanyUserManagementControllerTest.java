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
import tech.cspioneer.backend.service.CompanyUserManagementService;
import tech.cspioneer.backend.utils.JwtUtils;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompanyUserManagementController.class)
public class CompanyUserManagementControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private CompanyUserManagementService companyUserManagementService;
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
    // TODO: 详细补全所有接口的正常、异常、权限、参数校验、边界、未认证、未知身份、业务异常、系统异常等测试用例
    // region 获取公司用户
    @Test
    @DisplayName("获取公司用户-正常")
    void testGetCompanyUsers_Success() throws Exception {
        when(companyUserManagementService.getCompanyUsers(anyInt(), anyInt(), any(), any(), any(), any(), any())).thenReturn(org.mockito.Mockito.mock(tech.cspioneer.backend.model.response.PagedResponse.class));
        mockMvc.perform(get("/api/admin/company/users?page=1&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    @Test
    @DisplayName("获取公司用户-服务异常")
    void testGetCompanyUsers_Exception() throws Exception {
        when(companyUserManagementService.getCompanyUsers(anyInt(), anyInt(), any(), any(), any(), any(), any())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(get("/api/admin/company/users?page=1&size=5"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(5000));
    }
    // endregion

    // region 创建公司用户
    @Test
    @DisplayName("创建公司用户-正常")
    void testCreateCompanyUser_Success() throws Exception {
        var req = new tech.cspioneer.backend.entity.dto.request.UserCreateRequest();
        when(companyUserManagementService.createCompanyUser(any(), anyString())).thenReturn(org.mockito.Mockito.mock(tech.cspioneer.backend.entity.dto.response.UserResponse.class));
        mockMvc.perform(post("/api/admin/company/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201));
    }
    @Test
    @DisplayName("创建公司用户-业务异常")
    void testCreateCompanyUser_BizError() throws Exception {
        var req = new tech.cspioneer.backend.entity.dto.request.UserCreateRequest();
        when(companyUserManagementService.createCompanyUser(any(), anyString())).thenThrow(new tech.cspioneer.backend.exception.UserManagementException("bad req"));
        mockMvc.perform(post("/api/admin/company/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
    @Test
    @DisplayName("创建公司用户-系统异常")
    void testCreateCompanyUser_Exception() throws Exception {
        var req = new tech.cspioneer.backend.entity.dto.request.UserCreateRequest();
        when(companyUserManagementService.createCompanyUser(any(), anyString())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(post("/api/admin/company/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(5000));
    }
    // endregion

    // region 更新公司用户
    @Test
    @DisplayName("更新公司用户-正常")
    void testUpdateCompanyUser_Success() throws Exception {
        var req = new tech.cspioneer.backend.entity.dto.request.UserUpdateRequest();
        when(companyUserManagementService.updateCompanyUser(anyString(), any(), anyString())).thenReturn(org.mockito.Mockito.mock(tech.cspioneer.backend.entity.dto.response.UserResponse.class));
        mockMvc.perform(put("/api/admin/company/users/uuid1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    @Test
    @DisplayName("更新公司用户-未找到")
    void testUpdateCompanyUser_NotFound() throws Exception {
        var req = new tech.cspioneer.backend.entity.dto.request.UserUpdateRequest();
        when(companyUserManagementService.updateCompanyUser(anyString(), any(), anyString())).thenThrow(new tech.cspioneer.backend.exception.ResourceNotFoundException("not found"));
        mockMvc.perform(put("/api/admin/company/users/uuid1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
    @Test
    @DisplayName("更新公司用户-业务异常")
    void testUpdateCompanyUser_BizError() throws Exception {
        var req = new tech.cspioneer.backend.entity.dto.request.UserUpdateRequest();
        when(companyUserManagementService.updateCompanyUser(anyString(), any(), anyString())).thenThrow(new tech.cspioneer.backend.exception.UserManagementException("bad req"));
        mockMvc.perform(put("/api/admin/company/users/uuid1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
    @Test
    @DisplayName("更新公司用户-系统异常")
    void testUpdateCompanyUser_Exception() throws Exception {
        var req = new tech.cspioneer.backend.entity.dto.request.UserUpdateRequest();
        when(companyUserManagementService.updateCompanyUser(anyString(), any(), anyString())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(put("/api/admin/company/users/uuid1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(5000));
    }
    // endregion

    // region 删除公司用户
    @Test
    @DisplayName("删除公司用户-正常")
    void testDeleteCompanyUser_Success() throws Exception {
        mockMvc.perform(delete("/api/admin/company/users/uuid1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(204));
    }
    @Test
    @DisplayName("删除公司用户-未找到")
    void testDeleteCompanyUser_NotFound() throws Exception {
        org.mockito.Mockito.doThrow(new tech.cspioneer.backend.exception.ResourceNotFoundException("not found")).when(companyUserManagementService).deleteCompanyUser(anyString(), anyString());
        mockMvc.perform(delete("/api/admin/company/users/uuid1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
    @Test
    @DisplayName("删除公司用户-系统异常")
    void testDeleteCompanyUser_Exception() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("error")).when(companyUserManagementService).deleteCompanyUser(anyString(), anyString());
        mockMvc.perform(delete("/api/admin/company/users/uuid1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(5000));
    }
    // endregion

    // region 移除公司成员
    @Test
    @DisplayName("移除公司成员-正常")
    void testRemoveUserFromCompany_Success() throws Exception {
        mockMvc.perform(post("/api/admin/company/users/uuid1/remove"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    @Test
    @DisplayName("移除公司成员-未找到")
    void testRemoveUserFromCompany_NotFound() throws Exception {
        org.mockito.Mockito.doThrow(new tech.cspioneer.backend.exception.ResourceNotFoundException("not found")).when(companyUserManagementService).removeUserFromCompany(anyString(), anyString());
        mockMvc.perform(post("/api/admin/company/users/uuid1/remove"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
    @Test
    @DisplayName("移除公司成员-系统异常")
    void testRemoveUserFromCompany_Exception() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("error")).when(companyUserManagementService).removeUserFromCompany(anyString(), anyString());
        mockMvc.perform(post("/api/admin/company/users/uuid1/remove"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(5000));
    }
    // endregion

    // region 获取用户详情
    @Test
    @DisplayName("获取用户详情-正常")
    void testGetUserDetails_Success() throws Exception {
        when(companyUserManagementService.getUserDetails(anyString(), anyString())).thenReturn(org.mockito.Mockito.mock(tech.cspioneer.backend.entity.dto.response.UserDetailResponse.class));
        mockMvc.perform(get("/api/admin/company/users/uuid1/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    @Test
    @DisplayName("获取用户详情-未找到")
    void testGetUserDetails_NotFound() throws Exception {
        when(companyUserManagementService.getUserDetails(anyString(), anyString())).thenThrow(new tech.cspioneer.backend.exception.ResourceNotFoundException("not found"));
        mockMvc.perform(get("/api/admin/company/users/uuid1/details"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
    @Test
    @DisplayName("获取用户详情-无权限")
    void testGetUserDetails_Forbidden() throws Exception {
        when(companyUserManagementService.getUserDetails(anyString(), anyString())).thenThrow(new org.springframework.security.access.AccessDeniedException("forbidden"));
        mockMvc.perform(get("/api/admin/company/users/uuid1/details"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }
    @Test
    @DisplayName("获取用户详情-系统异常")
    void testGetUserDetails_Exception() throws Exception {
        when(companyUserManagementService.getUserDetails(anyString(), anyString())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(get("/api/admin/company/users/uuid1/details"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(5000));
    }
    // endregion
} 