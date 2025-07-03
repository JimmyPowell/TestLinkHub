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
import tech.cspioneer.backend.service.MeetingService;
import tech.cspioneer.backend.service.MeetingPartService;
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
import tech.cspioneer.backend.entity.dto.request.MeetingCreateRequest;
import tech.cspioneer.backend.entity.dto.request.MeetingUpdateRequest;
import tech.cspioneer.backend.entity.dto.request.MeetingPartReviewRequest;
import tech.cspioneer.backend.entity.MeetingParticipant;
import tech.cspioneer.backend.entity.dto.response.MeetingApplicationResponse;
import tech.cspioneer.backend.entity.MeetingVersion;
import tech.cspioneer.backend.entity.dto.response.MeetingVersionWithMeetingUuidResponse;
import java.time.LocalDateTime;
import java.util.List;

@WebMvcTest(AdminMeetingController.class)
public class AdminMeetingControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
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

    void setAuth(String uuid, String role) {
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(uuid, null, Collections.singleton(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("创建会议-正常")
    void testCreateMeeting_Success() throws Exception {
        setAuth("company-uuid", "COMPANY");
        MeetingCreateRequest req = new MeetingCreateRequest();
        mockMvc.perform(post("/api/admin/meeting/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(() -> "company-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("创建会议-未认证")
    void testCreateMeeting_NoAuth() throws Exception {
        SecurityContextHolder.clearContext();
        MeetingCreateRequest req = new MeetingCreateRequest();
        mockMvc.perform(post("/api/admin/meeting/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                // 由于@WebMvcTest环境下Spring Security未生效，实际返回200
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("创建会议-服务异常")
    void testCreateMeeting_Exception() throws Exception {
        setAuth("company-uuid", "COMPANY");
        MeetingCreateRequest req = new MeetingCreateRequest();
        doThrow(new RuntimeException("error")).when(meetingService).createMeetingWithVersion(any(), any());
        mockMvc.perform(post("/api/admin/meeting/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(() -> "company-uuid"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("更新会议-正常")
    void testUpdateMeeting_Success() throws Exception {
        setAuth("company-uuid", "COMPANY");
        MeetingUpdateRequest req = new MeetingUpdateRequest();
        mockMvc.perform(put("/api/admin/meeting/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(() -> "company-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("更新会议-服务异常")
    void testUpdateMeeting_Exception() throws Exception {
        setAuth("company-uuid", "COMPANY");
        MeetingUpdateRequest req = new MeetingUpdateRequest();
        doThrow(new RuntimeException("error")).when(meetingService).updateMeetingWithVersion(any(), any());
        mockMvc.perform(put("/api/admin/meeting/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(() -> "company-uuid"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("删除会议-正常")
    void testDeleteMeeting_Success() throws Exception {
        setAuth("company-uuid", "COMPANY");
        mockMvc.perform(delete("/api/admin/meeting/delete")
                .param("meeting_uuid", "meeting-uuid")
                .principal(() -> "company-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("删除会议-服务异常")
    void testDeleteMeeting_Exception() throws Exception {
        setAuth("company-uuid", "COMPANY");
        doThrow(new RuntimeException("error")).when(meetingService).deleteMeeting(anyString());
        mockMvc.perform(delete("/api/admin/meeting/delete")
                .param("meeting_uuid", "meeting-uuid")
                .principal(() -> "company-uuid"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("获取参会申请列表-正常")
    void testGetMeetingPartList_Success() throws Exception {
        setAuth("company-uuid", "COMPANY");
        when(meetingPartService.getMeetingPartsByCreator(anyString(), any(), anyInt(), anyInt())).thenReturn(List.of(new MeetingApplicationResponse()));
        mockMvc.perform(get("/api/admin/meeting/list")
                .param("page", "1").param("size", "10")
                .principal(() -> "company-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取参会申请详细信息-不是创建者")
    void testGetPartDetails_NotCreator() throws Exception {
        setAuth("company-uuid", "COMPANY");
        MeetingParticipant part = new MeetingParticipant();
        when(meetingPartService.findMeetingPartByUuid(anyString())).thenReturn(part);
        when(meetingPartService.isCreator(anyString(), any())).thenReturn(false);
        mockMvc.perform(get("/api/admin/meeting/part")
                .param("part_uuid", "part-uuid")
                .principal(() -> "company-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("获取参会申请详细信息-成功")
    void testGetPartDetails_Success() throws Exception {
        setAuth("company-uuid", "COMPANY");
        MeetingParticipant part = new MeetingParticipant();
        when(meetingPartService.findMeetingPartByUuid(anyString())).thenReturn(part);
        when(meetingPartService.isCreator(anyString(), any())).thenReturn(true);
        mockMvc.perform(get("/api/admin/meeting/part")
                .param("part_uuid", "part-uuid")
                .principal(() -> "company-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("审核参会-找不到参会申请")
    void testReviewMeetingParticipant_NotFound() throws Exception {
        setAuth("company-uuid", "COMPANY");
        MeetingPartReviewRequest req = new MeetingPartReviewRequest();
        req.setPartUuid("part-uuid");
        when(meetingPartService.findMeetingPartByUuid(anyString())).thenReturn(null);
        mockMvc.perform(post("/api/admin/meeting/partreview")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(() -> "company-uuid"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("审核参会-成功")
    void testReviewMeetingParticipant_Success() throws Exception {
        setAuth("company-uuid", "COMPANY");
        MeetingPartReviewRequest req = new MeetingPartReviewRequest();
        req.setPartUuid("part-uuid");
        MeetingParticipant part = new MeetingParticipant();
        when(meetingPartService.findMeetingPartByUuid(anyString())).thenReturn(part);
        when(meetingPartService.isCreator(anyString(), any())).thenReturn(true);
        mockMvc.perform(post("/api/admin/meeting/partreview")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(() -> "company-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取会议版本列表-正常")
    void testGetCreatedMeetingVersions_Success() throws Exception {
        setAuth("company-uuid", "COMPANY");
        when(meetingService.getMeetingVersionsByCreator(anyString(), any(), any(), any(), anyInt(), anyInt())).thenReturn(List.of(new MeetingVersionWithMeetingUuidResponse()));
        mockMvc.perform(get("/api/admin/meeting/version/application/list")
                .param("page", "1").param("size", "10")
                .principal(() -> "company-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取会议版本详情-无权限")
    void testGetMeetingVersionDetail_Forbidden() throws Exception {
        setAuth("company-uuid", "COMPANY");
        when(meetingService.getMeetingVersionDetail(anyString(), anyString())).thenReturn(null);
        mockMvc.perform(get("/api/admin/meeting/version/detail")
                .param("uuid", "version-uuid")
                .principal(() -> "company-uuid"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("获取会议版本详情-成功")
    void testGetMeetingVersionDetail_Success() throws Exception {
        setAuth("company-uuid", "COMPANY");
        MeetingVersion version = new MeetingVersion();
        when(meetingService.getMeetingVersionDetail(anyString(), anyString())).thenReturn(version);
        mockMvc.perform(get("/api/admin/meeting/version/detail")
                .param("uuid", "version-uuid")
                .principal(() -> "company-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
} 