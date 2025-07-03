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
import tech.cspioneer.backend.service.NotificationService;
import tech.cspioneer.backend.utils.JwtUtils;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.data.domain.PageImpl;
import java.util.Collections;

@WebMvcTest(AdminNotificationController.class)
public class AdminNotificationControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private NotificationService notificationService;
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
    @Test
    @DisplayName("分页获取通知-正常")
    void testGetAllNotifications_Success() throws Exception {
        when(notificationService.getAllNotifications(anyInt(), anyInt())).thenReturn(new PageImpl<>(Collections.emptyList()));
        mockMvc.perform(get("/api/admin/notifications?page=1&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    @Test
    @DisplayName("分页获取通知-异常")
    void testGetAllNotifications_Exception() throws Exception {
        when(notificationService.getAllNotifications(anyInt(), anyInt())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(get("/api/admin/notifications?page=1&size=5"))
                .andExpect(status().isInternalServerError());
    }
    @Test
    @DisplayName("发送公告-全部用户-正常")
    void testSendAnnouncement_All_Success() throws Exception {
        var req = new tech.cspioneer.backend.entity.dto.request.AnnouncementRequest();
        req.setTitle("t"); req.setContent("c"); req.setTargetAudience("ALL");
        mockMvc.perform(post("/api/admin/notifications/announcements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(202));
    }
    @Test
    @DisplayName("发送公告-公司-正常")
    void testSendAnnouncement_Company_Success() throws Exception {
        var req = new tech.cspioneer.backend.entity.dto.request.AnnouncementRequest();
        req.setTitle("t"); req.setContent("c"); req.setTargetAudience("COMPANY"); req.setCompanyId(1L);
        mockMvc.perform(post("/api/admin/notifications/announcements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(202));
    }
    @Test
    @DisplayName("发送公告-参数错误")
    void testSendAnnouncement_BadRequest() throws Exception {
        var req = new tech.cspioneer.backend.entity.dto.request.AnnouncementRequest();
        req.setTitle("t"); req.setContent("c"); req.setTargetAudience("COMPANY"); // companyId缺失
        mockMvc.perform(post("/api/admin/notifications/announcements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
    @Test
    @DisplayName("发送公告-系统异常")
    void testSendAnnouncement_Exception() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("error")).when(notificationService).sendSystemNotificationToAll(anyString(), anyString(), any(), any());
        var req = new tech.cspioneer.backend.entity.dto.request.AnnouncementRequest();
        req.setTitle("t"); req.setContent("c"); req.setTargetAudience("ALL");
        mockMvc.perform(post("/api/admin/notifications/announcements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }
} 