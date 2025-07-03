package tech.cspioneer.backend.controller;

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
import tech.cspioneer.backend.service.MeetingPartService;
import tech.cspioneer.backend.service.MeetingService;
import tech.cspioneer.backend.utils.JwtUtils;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;

@WebMvcTest(MeetingController.class)
public class MeetingControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private MeetingService meetingService;
    @MockBean
    private MeetingPartService meetingPartService;
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
    @DisplayName("会议浏览接口-正常")
    void testBrowseMeetings() throws Exception {
        when(meetingService.getPublishedMeetings(any(Integer.class), any(Integer.class))).thenReturn(java.util.Collections.emptyList());
        mockMvc.perform(get("/api/user/meeting/list?page=1&size=10")
                .principal(() -> "user-uuid"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("会议详情接口-正常")
    void testGetMeetingDetails() throws Exception {
        when(meetingService.getMeetingDetails(any(String.class))).thenReturn(null);
        mockMvc.perform(get("/api/user/meeting/?meeting_uuid=meeting-uuid")
                .principal(() -> "user-uuid"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("参会申请接口-正常")
    void testJoinMeeting() throws Exception {
        mockMvc.perform(post("/api/user/meeting/participant")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .principal(() -> "user-uuid"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("取消参会接口-正常")
    void testCancelMeetingParticipation() throws Exception {
        mockMvc.perform(delete("/api/user/meeting/participant/cancel?part_uuid=part-uuid")
                .principal(() -> "user-uuid"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("获取参会申请详情-正常")
    void testGetMeetingPartDetail_Success() throws Exception {
        when(meetingPartService.findMeetingPartByUser(anyString())).thenReturn(null);
        mockMvc.perform(get("/api/user/meeting/part?part_uuid=part-uuid")
                .principal(() -> "user-uuid"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("获取参会申请列表-正常")
    void testGetUserPartList_Success() throws Exception {
        when(meetingPartService.getMeetingPartsByUser(anyString(), anyInt(), anyInt())).thenReturn(java.util.Collections.emptyList());
        mockMvc.perform(get("/api/user/meeting/part/list?page=1&size=10")
                .principal(() -> "user-uuid"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("会议版本名称模糊查询-正常")
    void testSearchMeetingVersionsByName_Success() throws Exception {
        when(meetingService.searchMeetingVersionsByName(anyString(), anyInt(), anyInt())).thenReturn(java.util.Collections.emptyList());
        mockMvc.perform(get("/api/user/meeting/versions/search/by-name?name=test&page=1&size=10")
                .principal(() -> "user-uuid"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("会议版本UUID模糊查询-正常")
    void testSearchMeetingVersionsByUuid_Success() throws Exception {
        when(meetingService.searchMeetingVersionsByUuid(anyString(), anyInt(), anyInt())).thenReturn(java.util.Collections.emptyList());
        mockMvc.perform(get("/api/user/meeting/versions/search/by-uuid?uuid=test&page=1&size=10")
                .principal(() -> "user-uuid"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("会议版本开始时间范围查询-正常")
    void testSearchMeetingVersionsByStartTime_Success() throws Exception {
        when(meetingService.searchMeetingVersionsByStartTimeRange(any(), any(), anyInt(), anyInt())).thenReturn(java.util.Collections.emptyList());
        mockMvc.perform(get("/api/user/meeting/versions/search/by-start-time?start=2025-06-01T00:00:00&end=2025-06-30T23:59:59&page=1&size=10")
                .principal(() -> "user-uuid"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("参会申请-重复报名异常")
    void testJoinMeeting_Duplicate() throws Exception {
        doThrow(new IllegalArgumentException("用户已报名该会议")).when(meetingPartService).joinMeeting(any(), any());
        mockMvc.perform(post("/api/user/meeting/participant")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .principal(() -> "user-uuid"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    @DisplayName("参会申请-参数异常")
    void testJoinMeeting_BadRequest() throws Exception {
        doThrow(new IllegalArgumentException("参数错误")).when(meetingPartService).joinMeeting(any(), any());
        mockMvc.perform(post("/api/user/meeting/participant")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .principal(() -> "user-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("参会申请-服务器异常")
    void testJoinMeeting_ServerError() throws Exception {
        doThrow(new RuntimeException("服务器异常")).when(meetingPartService).joinMeeting(any(), any());
        mockMvc.perform(post("/api/user/meeting/participant")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .principal(() -> "user-uuid"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("取消参会-参数异常")
    void testCancelMeetingParticipation_BadRequest() throws Exception {
        doThrow(new IllegalArgumentException("参数错误")).when(meetingPartService).cancelParticipation(anyString(), anyString());
        mockMvc.perform(delete("/api/user/meeting/participant/cancel?part_uuid=part-uuid")
                .principal(() -> "user-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("取消参会-服务器异常")
    void testCancelMeetingParticipation_ServerError() throws Exception {
        doThrow(new RuntimeException("服务器异常")).when(meetingPartService).cancelParticipation(anyString(), anyString());
        mockMvc.perform(delete("/api/user/meeting/participant/cancel?part_uuid=part-uuid")
                .principal(() -> "user-uuid"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500));
    }
} 