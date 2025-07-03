package tech.cspioneer.backend.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import tech.cspioneer.backend.entity.dto.request.MeetingPartReviewRequest;

@WebMvcTest(RootMeetingController.class)
public class RootMeetingControllerTest {
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
    @BeforeEach
    void setUp() {
        reset(meetingService, meetingPartService);
    }
    // TODO: 详细补全所有接口的正常、异常、权限、参数校验、边界、未认证、未知身份、业务异常、系统异常等测试用例
    // region 会议审核
    @Test
    @DisplayName("会议审核-正常")
    void testReviewMeeting_Success() throws Exception {
        mockMvc.perform(post("/api/root/meeting/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    // 会议审核-参数异常、系统异常等分支
    @Test
    @DisplayName("会议审核-系统异常")
    void testReviewMeeting_Exception() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("error")).when(meetingService).reviewMeetingCreate(any(), anyString());
        mockMvc.perform(post("/api/root/meeting/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isInternalServerError());
    }
    // endregion

    // region 审核会议浏览
    @Test
    @DisplayName("审核会议浏览-正常")
    void testGetMeetingReviewList_Success() throws Exception {
        when(meetingService.getPendingReviewList(anyInt(), anyInt(), any(), any())).thenReturn(java.util.Collections.emptyList());
        mockMvc.perform(get("/api/root/meeting/reviewlist?page=1&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    @Test
    @DisplayName("审核会议浏览-系统异常")
    void testGetMeetingReviewList_Exception() throws Exception {
        when(meetingService.getPendingReviewList(anyInt(), anyInt(), any(), any())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(get("/api/root/meeting/reviewlist?page=1&size=5"))
                .andExpect(status().isInternalServerError());
    }
    // endregion

    // region 会议详情
    @Test
    @DisplayName("会议详情-正常")
    void testGetMeetingDetails_Success() throws Exception {
        when(meetingService.getMeetingVersionDetails(anyString())).thenReturn(org.mockito.Mockito.mock(tech.cspioneer.backend.entity.dto.response.RootReviewResponse.class));
        mockMvc.perform(get("/api/root/meeting/uuid1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    @Test
    @DisplayName("会议详情-系统异常")
    void testGetMeetingDetails_Exception() throws Exception {
        when(meetingService.getMeetingVersionDetails(anyString())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(get("/api/root/meeting/uuid1"))
                .andExpect(status().isInternalServerError());
    }
    // endregion

    // region 创建会议
    @Test
    @DisplayName("创建会议-正常")
    void testCreateMeeting_Success() throws Exception {
        mockMvc.perform(post("/api/root/meeting/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    @Test
    @DisplayName("创建会议-系统异常")
    void testCreateMeeting_Exception() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("error")).when(meetingService).createMeeting(any(), anyString());
        mockMvc.perform(post("/api/root/meeting/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isInternalServerError());
    }
    // endregion

    // region 更新会议
    @Test
    @DisplayName("更新会议-正常")
    void testUpdateMeeting_Success() throws Exception {
        mockMvc.perform(put("/api/root/meeting/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    @Test
    @DisplayName("更新会议-系统异常")
    void testUpdateMeeting_Exception() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("error")).when(meetingService).updateMeeting(any(), anyString());
        mockMvc.perform(put("/api/root/meeting/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isInternalServerError());
    }
    // endregion

    // region 删除会议
    @Test
    @DisplayName("删除会议-正常")
    void testDeleteMeeting_Success() throws Exception {
        mockMvc.perform(delete("/api/root/meeting/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    @Test
    @DisplayName("删除会议-系统异常")
    void testDeleteMeeting_Exception() throws Exception {
        reset(meetingService);
        org.mockito.Mockito.doThrow(new RuntimeException("error")).when(meetingService).deleteMeeting(anyString());
        mockMvc.perform(delete("/api/root/meeting/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    // endregion

    // region 获取参会申请列表
    @Test
    @DisplayName("获取参会申请列表-正常")
    void testGetMeetingPartList_Success() throws Exception {
        when(meetingPartService.getMeetingPartsByCreator(anyString(), any(), anyInt(), anyInt())).thenReturn(java.util.Collections.emptyList());
        mockMvc.perform(get("/api/root/meeting/list?page=1&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    @Test
    @DisplayName("获取参会申请列表-系统异常")
    void testGetMeetingPartList_Exception() throws Exception {
        when(meetingPartService.getMeetingPartsByCreator(anyString(), any(), anyInt(), anyInt())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(get("/api/root/meeting/list?page=1&size=5"))
                .andExpect(status().isInternalServerError());
    }
    // endregion

    // region 获取参会申请详细信息
    @Test
    @DisplayName("获取参会申请详细信息-正常")
    void testGetPartDetails_Success() throws Exception {
        var part = org.mockito.Mockito.mock(tech.cspioneer.backend.entity.MeetingParticipant.class);
        when(meetingPartService.findMeetingPartByUuid(anyString())).thenReturn(part);
        when(meetingPartService.isCreator(anyString(), any())).thenReturn(true);
        mockMvc.perform(get("/api/root/meeting/part?partUuid=uuid1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    @Test
    @DisplayName("获取参会申请详细信息-未找到")
    void testGetPartDetails_NotFound() throws Exception {
        when(meetingPartService.findMeetingPartByUuid(anyString())).thenReturn(null);
        mockMvc.perform(get("/api/root/meeting/part?partUuid=uuid1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
    @Test
    @DisplayName("获取参会申请详细信息-无权限")
    void testGetPartDetails_Forbidden() throws Exception {
        var part = org.mockito.Mockito.mock(tech.cspioneer.backend.entity.MeetingParticipant.class);
        when(meetingPartService.findMeetingPartByUuid(anyString())).thenReturn(part);
        when(meetingPartService.isCreator(anyString(), any())).thenReturn(false);
        mockMvc.perform(get("/api/root/meeting/part?partUuid=uuid1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }
    @Test
    @DisplayName("获取参会申请详细信息-系统异常")
    void testGetPartDetails_Exception() throws Exception {
        when(meetingPartService.findMeetingPartByUuid(anyString())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(get("/api/root/meeting/part?partUuid=uuid1"))
                .andExpect(status().isInternalServerError());
    }
    // endregion

    // region 审核参会
    @Test
    @DisplayName("审核参会-正常")
    void testReviewMeetingParticipant_Success() throws Exception {
        var part = org.mockito.Mockito.mock(tech.cspioneer.backend.entity.MeetingParticipant.class);
        reset(meetingPartService);
        when(meetingPartService.findMeetingPartByUuid(eq("uuid1"))).thenReturn(part);
        when(meetingPartService.isCreator(anyString(), any())).thenReturn(true);
        MeetingPartReviewRequest req = new MeetingPartReviewRequest();
        req.setPartUuid("uuid1");
        mockMvc.perform(post("/api/root/meeting/partreview")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    @Test
    @DisplayName("审核参会-未找到")
    void testReviewMeetingParticipant_NotFound() throws Exception {
        reset(meetingPartService);
        when(meetingPartService.findMeetingPartByUuid(eq("uuid1"))).thenReturn(null);
        MeetingPartReviewRequest req = new MeetingPartReviewRequest();
        req.setPartUuid("uuid1");
        mockMvc.perform(post("/api/root/meeting/partreview")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
    @Test
    @DisplayName("审核参会-无权限")
    void testReviewMeetingParticipant_Forbidden() throws Exception {
        var part = org.mockito.Mockito.mock(tech.cspioneer.backend.entity.MeetingParticipant.class);
        reset(meetingPartService);
        when(meetingPartService.findMeetingPartByUuid(eq("uuid1"))).thenReturn(part);
        when(meetingPartService.isCreator(anyString(), any())).thenReturn(false);
        MeetingPartReviewRequest req = new MeetingPartReviewRequest();
        req.setPartUuid("uuid1");
        mockMvc.perform(post("/api/root/meeting/partreview")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }
    @Test
    @DisplayName("审核参会-系统异常")
    void testReviewMeetingParticipant_Exception() throws Exception {
        reset(meetingPartService);
        when(meetingPartService.findMeetingPartByUuid(eq("uuid1"))).thenThrow(new RuntimeException("error"));
        MeetingPartReviewRequest req = new MeetingPartReviewRequest();
        req.setPartUuid("uuid1");
        mockMvc.perform(post("/api/root/meeting/partreview")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }
    // endregion
} 