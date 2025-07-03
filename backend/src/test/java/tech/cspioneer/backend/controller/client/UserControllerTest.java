package tech.cspioneer.backend.controller.client;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import tech.cspioneer.backend.entity.dto.request.UpdateUserRequest;
import tech.cspioneer.backend.entity.dto.response.UserInfoResponse;
import tech.cspioneer.backend.exception.ResourceNotFoundException;
import tech.cspioneer.backend.service.UserService;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    @MockBean
    private tech.cspioneer.backend.utils.JwtUtils jwtUtils;
    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable());
            return http.build();
        }
    }

    private void setAuth() {
        Authentication auth = new UsernamePasswordAuthenticationToken("mock-uuid", null, Collections.singleton(new SimpleGrantedAuthority("USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("getCurrentUser-正常")
    void testGetCurrentUser_Success() throws Exception {
        setAuth();
        when(userService.getUserInfo(anyString())).thenReturn(new UserInfoResponse());
        mockMvc.perform(get("/api/client/user/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("getCurrentUser-未找到")
    void testGetCurrentUser_NotFound() throws Exception {
        setAuth();
        when(userService.getUserInfo(anyString())).thenThrow(new ResourceNotFoundException("not found"));
        mockMvc.perform(get("/api/client/user/me"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("getCurrentUser-系统异常")
    void testGetCurrentUser_Exception() throws Exception {
        setAuth();
        when(userService.getUserInfo(anyString())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(get("/api/client/user/me"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("updateCurrentUser-正常")
    void testUpdateCurrentUser_Success() throws Exception {
        setAuth();
        UpdateUserRequest req = new UpdateUserRequest();
        when(userService.updateUserInfo(anyString(), any())).thenReturn(new UserInfoResponse());
        mockMvc.perform(put("/api/client/user/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("updateCurrentUser-未找到")
    void testUpdateCurrentUser_NotFound() throws Exception {
        setAuth();
        UpdateUserRequest req = new UpdateUserRequest();
        when(userService.updateUserInfo(anyString(), any())).thenThrow(new ResourceNotFoundException("not found"));
        mockMvc.perform(put("/api/client/user/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("updateCurrentUser-系统异常")
    void testUpdateCurrentUser_Exception() throws Exception {
        setAuth();
        UpdateUserRequest req = new UpdateUserRequest();
        when(userService.updateUserInfo(anyString(), any())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(put("/api/client/user/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500));
    }
} 