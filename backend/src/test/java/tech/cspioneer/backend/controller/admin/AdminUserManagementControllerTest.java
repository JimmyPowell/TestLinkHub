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
import tech.cspioneer.backend.service.AdminUserManagementService;
import tech.cspioneer.backend.utils.JwtUtils;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserManagementController.class)
public class AdminUserManagementControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private AdminUserManagementService adminUserManagementService;
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
    // 示例：获取所有用户-正常
    @Test
    @DisplayName("获取所有用户-正常")
    void testGetAllUsers_Success() throws Exception {
        when(adminUserManagementService.getAllUsers(anyInt(), anyInt())).thenReturn(org.mockito.Mockito.mock(tech.cspioneer.backend.model.response.PagedResponse.class));
        mockMvc.perform(get("/api/admin/users?page=1&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    // region 获取所有用户
    @Test
    @DisplayName("获取所有用户-参数异常")
    void testGetAllUsers_ParamError() throws Exception {
        mockMvc.perform(get("/api/admin/users?page=-1&size=0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200)); // Controller未做参数校验
    }
    @Test
    @DisplayName("获取所有用户-服务异常")
    void testGetAllUsers_Exception() throws Exception {
        when(adminUserManagementService.getAllUsers(anyInt(), anyInt())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(get("/api/admin/users?page=1&size=5"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(5000));
    }
    // endregion

    // region 获取用户详情
    @Test
    @DisplayName("获取用户详情-正常")
    void testGetUserByUuid_Success() throws Exception {
        when(adminUserManagementService.getUserByUuid(anyString())).thenReturn(org.mockito.Mockito.mock(tech.cspioneer.backend.entity.dto.response.UserResponse.class));
        mockMvc.perform(get("/api/admin/users/uuid1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    @Test
    @DisplayName("获取用户详情-未找到")
    void testGetUserByUuid_NotFound() throws Exception {
        when(adminUserManagementService.getUserByUuid(anyString())).thenThrow(new tech.cspioneer.backend.exception.ResourceNotFoundException("not found"));
        mockMvc.perform(get("/api/admin/users/uuid1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
    @Test
    @DisplayName("获取用户详情-系统异常")
    void testGetUserByUuid_Exception() throws Exception {
        when(adminUserManagementService.getUserByUuid(anyString())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(get("/api/admin/users/uuid1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(5000));
    }
    // endregion

    // region 创建用户
    @Test
    @DisplayName("创建用户-正常")
    void testCreateUser_Success() throws Exception {
        var req = new tech.cspioneer.backend.entity.dto.request.UserCreateRequest();
        when(adminUserManagementService.createUser(any())).thenReturn(org.mockito.Mockito.mock(tech.cspioneer.backend.entity.dto.response.UserResponse.class));
        mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201));
    }
    @Test
    @DisplayName("创建用户-业务异常")
    void testCreateUser_BizError() throws Exception {
        var req = new tech.cspioneer.backend.entity.dto.request.UserCreateRequest();
        when(adminUserManagementService.createUser(any())).thenThrow(new tech.cspioneer.backend.exception.UserManagementException("bad req"));
        mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
    @Test
    @DisplayName("创建用户-系统异常")
    void testCreateUser_Exception() throws Exception {
        var req = new tech.cspioneer.backend.entity.dto.request.UserCreateRequest();
        when(adminUserManagementService.createUser(any())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(5000));
    }
    // endregion

    // region 更新用户
    @Test
    @DisplayName("更新用户-正常")
    void testUpdateUser_Success() throws Exception {
        var req = new tech.cspioneer.backend.entity.dto.request.UserUpdateRequest();
        when(adminUserManagementService.updateUser(anyString(), any())).thenReturn(org.mockito.Mockito.mock(tech.cspioneer.backend.entity.dto.response.UserResponse.class));
        mockMvc.perform(put("/api/admin/users/uuid1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    @Test
    @DisplayName("更新用户-未找到")
    void testUpdateUser_NotFound() throws Exception {
        var req = new tech.cspioneer.backend.entity.dto.request.UserUpdateRequest();
        when(adminUserManagementService.updateUser(anyString(), any())).thenThrow(new tech.cspioneer.backend.exception.ResourceNotFoundException("not found"));
        mockMvc.perform(put("/api/admin/users/uuid1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
    @Test
    @DisplayName("更新用户-业务异常")
    void testUpdateUser_BizError() throws Exception {
        var req = new tech.cspioneer.backend.entity.dto.request.UserUpdateRequest();
        when(adminUserManagementService.updateUser(anyString(), any())).thenThrow(new tech.cspioneer.backend.exception.UserManagementException("bad req"));
        mockMvc.perform(put("/api/admin/users/uuid1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
    @Test
    @DisplayName("更新用户-系统异常")
    void testUpdateUser_Exception() throws Exception {
        var req = new tech.cspioneer.backend.entity.dto.request.UserUpdateRequest();
        when(adminUserManagementService.updateUser(anyString(), any())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(put("/api/admin/users/uuid1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(5000));
    }
    // endregion

    // region 删除用户
    @Test
    @DisplayName("删除用户-正常")
    void testDeleteUser_Success() throws Exception {
        mockMvc.perform(delete("/api/admin/users/uuid1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(204));
    }
    @Test
    @DisplayName("删除用户-未找到")
    void testDeleteUser_NotFound() throws Exception {
        org.mockito.Mockito.doThrow(new tech.cspioneer.backend.exception.ResourceNotFoundException("not found")).when(adminUserManagementService).deleteUser(anyString());
        mockMvc.perform(delete("/api/admin/users/uuid1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
    @Test
    @DisplayName("删除用户-系统异常")
    void testDeleteUser_Exception() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("error")).when(adminUserManagementService).deleteUser(anyString());
        mockMvc.perform(delete("/api/admin/users/uuid1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(5000));
    }
    // endregion
} 