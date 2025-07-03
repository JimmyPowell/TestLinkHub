package tech.cspioneer.backend.controller.root;

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
import tech.cspioneer.backend.entity.dto.request.RootUserSearchRequest;
import tech.cspioneer.backend.entity.dto.request.UserCreateRequest;
import tech.cspioneer.backend.entity.dto.request.UserUpdateRequest;
import tech.cspioneer.backend.entity.dto.response.UserResponse;
import tech.cspioneer.backend.exception.ResourceNotFoundException;
import tech.cspioneer.backend.exception.UserManagementException;
import tech.cspioneer.backend.model.response.PagedResponse;
import tech.cspioneer.backend.service.RootUserManagementService;
import tech.cspioneer.backend.utils.JwtUtils;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RootUserManagementController.class)
public class RootUserManagementControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private RootUserManagementService userService;
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

    @Test
    @DisplayName("获取用户列表-正常")
    void testGetAllUsers_Success() throws Exception {
        when(userService.getAllUsers(anyInt(), anyInt(), any())).thenReturn(new PagedResponse<>(Collections.emptyList(), 0, 1, 0, 0, false));
        mockMvc.perform(get("/api/root/users?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取用户列表-系统异常")
    void testGetAllUsers_Exception() throws Exception {
        when(userService.getAllUsers(anyInt(), anyInt(), any())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(get("/api/root/users?page=0&size=10"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(5000));
    }

    @Test
    @DisplayName("获取用户详情-正常")
    void testGetUserByUuid_Success() throws Exception {
        when(userService.getUserByUuid(anyString())).thenReturn(new UserResponse());
        mockMvc.perform(get("/api/root/users/uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取用户详情-未找到")
    void testGetUserByUuid_NotFound() throws Exception {
        when(userService.getUserByUuid(anyString())).thenThrow(new ResourceNotFoundException("not found"));
        mockMvc.perform(get("/api/root/users/uuid"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("获取用户详情-系统异常")
    void testGetUserByUuid_Exception() throws Exception {
        when(userService.getUserByUuid(anyString())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(get("/api/root/users/uuid"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(5000));
    }

    @Test
    @DisplayName("创建用户-正常")
    void testCreateUser_Success() throws Exception {
        UserCreateRequest req = new UserCreateRequest();
        when(userService.createUser(any())).thenReturn(new UserResponse());
        mockMvc.perform(post("/api/root/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201));
    }

    @Test
    @DisplayName("创建用户-业务异常")
    void testCreateUser_BadRequest() throws Exception {
        UserCreateRequest req = new UserCreateRequest();
        when(userService.createUser(any())).thenThrow(new UserManagementException("error"));
        mockMvc.perform(post("/api/root/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("创建用户-系统异常")
    void testCreateUser_Exception() throws Exception {
        UserCreateRequest req = new UserCreateRequest();
        when(userService.createUser(any())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(post("/api/root/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(5000));
    }

    @Test
    @DisplayName("更新用户-正常")
    void testUpdateUser_Success() throws Exception {
        UserUpdateRequest req = new UserUpdateRequest();
        when(userService.updateUser(anyString(), any())).thenReturn(new UserResponse());
        mockMvc.perform(put("/api/root/users/uuid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("更新用户-未找到")
    void testUpdateUser_NotFound() throws Exception {
        UserUpdateRequest req = new UserUpdateRequest();
        when(userService.updateUser(anyString(), any())).thenThrow(new ResourceNotFoundException("not found"));
        mockMvc.perform(put("/api/root/users/uuid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("更新用户-业务异常")
    void testUpdateUser_BadRequest() throws Exception {
        UserUpdateRequest req = new UserUpdateRequest();
        when(userService.updateUser(anyString(), any())).thenThrow(new UserManagementException("error"));
        mockMvc.perform(put("/api/root/users/uuid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("更新用户-系统异常")
    void testUpdateUser_Exception() throws Exception {
        UserUpdateRequest req = new UserUpdateRequest();
        when(userService.updateUser(anyString(), any())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(put("/api/root/users/uuid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(5000));
    }

    @Test
    @DisplayName("删除用户-正常")
    void testDeleteUser_Success() throws Exception {
        mockMvc.perform(delete("/api/root/users/uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(204));
    }

    @Test
    @DisplayName("删除用户-未找到")
    void testDeleteUser_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("not found")).when(userService).deleteUser(anyString());
        mockMvc.perform(delete("/api/root/users/uuid"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("删除用户-系统异常")
    void testDeleteUser_Exception() throws Exception {
        doThrow(new RuntimeException("error")).when(userService).deleteUser(anyString());
        mockMvc.perform(delete("/api/root/users/uuid"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(5000));
    }

    @Test
    @DisplayName("根据公司获取用户-正常")
    void testGetUsersByCompanyUuid_Success() throws Exception {
        when(userService.getAllUsers(anyInt(), anyInt(), any())).thenReturn(new PagedResponse<>(Collections.emptyList(), 0, 1, 0, 0, false));
        mockMvc.perform(get("/api/root/users/company/companyUuid?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("根据公司获取用户-系统异常")
    void testGetUsersByCompanyUuid_Exception() throws Exception {
        when(userService.getAllUsers(anyInt(), anyInt(), any())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(get("/api/root/users/company/companyUuid?page=0&size=10"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(5000));
    }
} 