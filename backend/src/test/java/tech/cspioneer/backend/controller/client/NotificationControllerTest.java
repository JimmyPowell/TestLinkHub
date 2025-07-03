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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import tech.cspioneer.backend.entity.dto.request.MarkNotificationsAsReadRequest;
import tech.cspioneer.backend.entity.dto.response.NotificationResponse;
import tech.cspioneer.backend.exception.ResourceNotFoundException;
import tech.cspioneer.backend.model.response.ApiResponse;
import tech.cspioneer.backend.service.NotificationService;
import org.springframework.data.domain.Page;
import java.util.Collections;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
public class NotificationControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private NotificationService notificationService;
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

    private void setUser() {
        UserDetails user = User.withUsername("mock-uuid").password("pwd").authorities("USER").build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    @Test
    @DisplayName("getNotifications-正常")
    void testGetNotifications_Success() throws Exception {
        setUser();
        Page<NotificationResponse> page = Page.empty();
        when(notificationService.getNotificationsByUserUuid(anyString(), anyString(), anyInt(), anyInt())).thenReturn(page);
        mockMvc.perform(get("/api/client/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("getNotificationDetails-正常")
    void testGetNotificationDetails_Success() throws Exception {
        setUser();
        when(notificationService.getNotificationDetails(anyString(), anyString())).thenReturn(new NotificationResponse());
        mockMvc.perform(get("/api/client/notifications/uuid-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("getNotificationDetails-未找到")
    void testGetNotificationDetails_NotFound() throws Exception {
        setUser();
        when(notificationService.getNotificationDetails(anyString(), anyString())).thenThrow(new ResourceNotFoundException("not found"));
        mockMvc.perform(get("/api/client/notifications/uuid-1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("markAsRead-正常")
    void testMarkAsRead_Success() throws Exception {
        setUser();
        mockMvc.perform(patch("/api/client/notifications/uuid-1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("markAsRead-未找到")
    void testMarkAsRead_NotFound() throws Exception {
        setUser();
        doThrow(new ResourceNotFoundException("not found")).when(notificationService).markNotificationAsRead(anyString(), anyString());
        mockMvc.perform(patch("/api/client/notifications/uuid-1/read"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("markBatchAsRead-正常")
    void testMarkBatchAsRead_Success() throws Exception {
        setUser();
        MarkNotificationsAsReadRequest req = new MarkNotificationsAsReadRequest();
        req.setNotificationUuids(List.of("uuid-1", "uuid-2"));
        mockMvc.perform(patch("/api/client/notifications/read")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("markBatchAsRead-未找到")
    void testMarkBatchAsRead_NotFound() throws Exception {
        setUser();
        MarkNotificationsAsReadRequest req = new MarkNotificationsAsReadRequest();
        req.setNotificationUuids(List.of("uuid-1", "uuid-2"));
        doThrow(new ResourceNotFoundException("not found")).when(notificationService).markNotificationsAsRead(anyList(), anyString());
        mockMvc.perform(patch("/api/client/notifications/read")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("markAllAsRead-正常")
    void testMarkAllAsRead_Success() throws Exception {
        setUser();
        mockMvc.perform(patch("/api/client/notifications/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("deleteNotification-正常")
    void testDeleteNotification_Success() throws Exception {
        setUser();
        mockMvc.perform(delete("/api/client/notifications/uuid-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(204));
    }

    @Test
    @DisplayName("deleteNotification-未找到")
    void testDeleteNotification_NotFound() throws Exception {
        setUser();
        doThrow(new ResourceNotFoundException("not found")).when(notificationService).deleteNotification(anyString(), anyString());
        mockMvc.perform(delete("/api/client/notifications/uuid-1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
} 