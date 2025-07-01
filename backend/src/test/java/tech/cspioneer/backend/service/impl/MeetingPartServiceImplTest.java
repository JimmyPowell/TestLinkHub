package tech.cspioneer.backend.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.cspioneer.backend.entity.*;
import tech.cspioneer.backend.entity.dto.request.MeetingPartReviewRequest;
import tech.cspioneer.backend.entity.dto.request.MeetingParticipantRequest;
import tech.cspioneer.backend.entity.dto.response.MeetingApplicationResponse;
import tech.cspioneer.backend.entity.dto.response.MeetingPartResponse;
import tech.cspioneer.backend.exception.ResourceNotFoundException;
import tech.cspioneer.backend.mapper.CompanyMapper;
import tech.cspioneer.backend.mapper.MeetingMapper;
import tech.cspioneer.backend.mapper.MeetingParticipantMapper;
import tech.cspioneer.backend.mapper.UserMapper;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingPartServiceImplTest {
    @Mock
    private MeetingMapper meetingMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private MeetingParticipantMapper meetingParticipantMapper;
    @Mock
    private CompanyMapper companyMapper;
    @InjectMocks
    private MeetingPartServiceImpl meetingPartService;

    private MeetingParticipant mockPart;
    private Meeting mockMeeting;
    private User mockUser;
    private Company mockCompany;

    @BeforeEach
    void setUp() {
        mockPart = MeetingParticipant.builder()
                .id(1L)
                .uuid("part-uuid")
                .meetingId(2L)
                .userId(3L)
                .joinReason("reason")
                .status("pending")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        mockMeeting = Meeting.builder()
                .id(2L)
                .uuid("meeting-uuid")
                .creatorId(4L)
                .status("draft")
                .isDeleted(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        mockUser = User.builder()
                .id(3L)
                .uuid("user-uuid")
                .name("user")
                .build();
        mockCompany = Company.builder()
                .id(4L)
                .uuid("company-uuid")
                .name("company")
                .build();
    }

    @Test
    void testReviewPart_Normal() {
        MeetingPartReviewRequest req = MeetingPartReviewRequest.builder()
                .partUuid("part-uuid").reviewResult("approved").comments("ok").build();
        when(meetingParticipantMapper.findPartByUuid(anyString())).thenReturn(mockPart);
        doNothing().when(meetingParticipantMapper).updateReviewResult(anyLong(), anyString(), anyString());
        assertDoesNotThrow(() -> meetingPartService.reviewpart(req));
        verify(meetingParticipantMapper).updateReviewResult(eq(1L), eq("approved"), eq("ok"));
    }

    @Test
    void testReviewPart_NotExist() {
        when(meetingParticipantMapper.findPartByUuid(anyString())).thenReturn(null);
        MeetingPartReviewRequest req = MeetingPartReviewRequest.builder().partUuid("not-exist").build();
        assertThrows(RuntimeException.class, () -> meetingPartService.reviewpart(req));
    }

    @Test
    void testReviewPart_NotPending() {
        MeetingParticipant part = new MeetingParticipant(
            mockPart.getId(), mockPart.getUuid(), mockPart.getMeetingId(), mockPart.getUserId(),
            mockPart.getJoinReason(), "approved", mockPart.getReviewComment(),
            mockPart.getCreatedAt(), mockPart.getUpdatedAt()
        );
        when(meetingParticipantMapper.findPartByUuid(anyString())).thenReturn(part);
        MeetingPartReviewRequest req = MeetingPartReviewRequest.builder().partUuid("part-uuid").build();
        assertThrows(RuntimeException.class, () -> meetingPartService.reviewpart(req));
    }

    @Test
    void testFindMeetingPartByUuid_Normal() {
        when(meetingParticipantMapper.findByUuid(anyString())).thenReturn(mockPart);
        MeetingParticipant result = meetingPartService.findMeetingPartByUuid("part-uuid");
        assertNotNull(result);
        assertEquals("part-uuid", result.getUuid());
    }

    @Test
    void testFindMeetingPartByUuid_NotExist() {
        when(meetingParticipantMapper.findByUuid(anyString())).thenReturn(null);
        MeetingParticipant result = meetingPartService.findMeetingPartByUuid("not-exist");
        assertNull(result);
    }

    @Test
    void testFindMeetingPartByUser_Normal() {
        when(meetingParticipantMapper.findByUuid(anyString())).thenReturn(mockPart);
        when(meetingMapper.findById(anyLong())).thenReturn(mockMeeting);
        MeetingPartResponse resp = meetingPartService.findMeetingPartByUser("part-uuid");
        assertNotNull(resp);
        assertEquals("part-uuid", resp.getUuid());
    }

    @Test
    void testFindMeetingPartByUser_NotExist() {
        when(meetingParticipantMapper.findByUuid(anyString())).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> meetingPartService.findMeetingPartByUser("not-exist"));
    }

    @Test
    void testIsCreator_True() {
        when(meetingMapper.findById(anyLong())).thenReturn(mockMeeting);
        when(companyMapper.findByUuid(anyString())).thenReturn(mockCompany);
        MeetingParticipant part = mockPart;
        assertTrue(meetingPartService.isCreator("company-uuid", part));
    }

    @Test
    void testIsCreator_False_MeetingNull() {
        when(meetingMapper.findById(anyLong())).thenReturn(null);
        MeetingParticipant part = mockPart;
        assertFalse(meetingPartService.isCreator("company-uuid", part));
    }

    @Test
    void testIsCreator_False_CompanyNull() {
        when(meetingMapper.findById(anyLong())).thenReturn(mockMeeting);
        when(companyMapper.findByUuid(anyString())).thenReturn(null);
        MeetingParticipant part = mockPart;
        assertFalse(meetingPartService.isCreator("company-uuid", part));
    }

    @Test
    void testIsCreator_False_IdNotMatch() {
        when(meetingMapper.findById(anyLong())).thenReturn(mockMeeting);
        Company other = new Company(
            999L, mockCompany.getUuid(), mockCompany.getName(), mockCompany.getEmail(),
            mockCompany.getPassword(), mockCompany.getPhoneNumber(), mockCompany.getAddress(),
            mockCompany.getAvatarUrl(), mockCompany.getCompanyCode(), mockCompany.getStatus(),
            mockCompany.getDescription(), mockCompany.getCreatedAt(), mockCompany.getUpdatedAt()
        );
        when(companyMapper.findByUuid(anyString())).thenReturn(other);
        MeetingParticipant part = mockPart;
        assertFalse(meetingPartService.isCreator("company-uuid", part));
    }

    @Test
    void testGetMeetingPartsByCreator_Normal() {
        when(companyMapper.findByUuid(anyString())).thenReturn(mockCompany);
        List<MeetingApplicationResponse> list = new ArrayList<>();
        when(meetingParticipantMapper.findPartsByCreator(anyLong(), any(), anyInt(), anyInt())).thenReturn(list);
        List<MeetingApplicationResponse> result = meetingPartService.getMeetingPartsByCreator("company-uuid", null, 1, 10);
        assertNotNull(result);
    }

    @Test
    void testGetMeetingPartsByCreator_CompanyNotExist() {
        when(companyMapper.findByUuid(anyString())).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> meetingPartService.getMeetingPartsByCreator("not-exist", null, 1, 10));
    }

    @Test
    void testGetMeetingPartsByUser_Normal() {
        when(userMapper.findByUuid(anyString())).thenReturn(mockUser);
        List<MeetingParticipant> parts = new ArrayList<>();
        parts.add(mockPart);
        when(meetingParticipantMapper.findPartsByUser(anyLong(), anyInt(), anyInt())).thenReturn(parts);
        when(meetingMapper.findById(anyLong())).thenReturn(mockMeeting);
        List<MeetingPartResponse> result = meetingPartService.getMeetingPartsByUser("user-uuid", 1, 10);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetMeetingPartsByUser_UserNotExist() {
        when(userMapper.findByUuid(anyString())).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> meetingPartService.getMeetingPartsByUser("not-exist", 1, 10));
    }

    @Test
    void testGetMeetingPartsByUser_Empty() {
        when(userMapper.findByUuid(anyString())).thenReturn(mockUser);
        when(meetingParticipantMapper.findPartsByUser(anyLong(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
        List<MeetingPartResponse> result = meetingPartService.getMeetingPartsByUser("user-uuid", 1, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetMeetingPartsByUser_InvalidUserUuid() {
        assertThrows(IllegalArgumentException.class, () -> meetingPartService.getMeetingPartsByUser(" ", 1, 10));
    }

    @Test
    void testJoinMeeting_Normal() {
        MeetingParticipantRequest req = new MeetingParticipantRequest();
        req.setMeetingUuid("meeting-uuid");
        req.setJoinReason("reason");
        when(meetingMapper.findByUuid(anyString())).thenReturn(mockMeeting);
        when(userMapper.findByUuid(anyString())).thenReturn(mockUser);
        when(meetingParticipantMapper.existsByMeetingIdAndUserId(anyLong(), anyLong())).thenReturn(false);
        doNothing().when(meetingParticipantMapper).insertParticipant(any(MeetingParticipant.class));
        assertDoesNotThrow(() -> meetingPartService.joinMeeting(req, "user-uuid"));
    }

    @Test
    void testJoinMeeting_MeetingUuidNull() {
        MeetingParticipantRequest req = new MeetingParticipantRequest();
        req.setMeetingUuid(null);
        assertThrows(IllegalArgumentException.class, () -> meetingPartService.joinMeeting(req, "user-uuid"));
    }

    @Test
    void testJoinMeeting_MeetingNotExist() {
        MeetingParticipantRequest req = new MeetingParticipantRequest();
        req.setMeetingUuid("meeting-uuid");
        when(meetingMapper.findByUuid(anyString())).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> meetingPartService.joinMeeting(req, "user-uuid"));
    }

    @Test
    void testJoinMeeting_UserNotExist() {
        MeetingParticipantRequest req = new MeetingParticipantRequest();
        req.setMeetingUuid("meeting-uuid");
        when(meetingMapper.findByUuid(anyString())).thenReturn(mockMeeting);
        when(userMapper.findByUuid(anyString())).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> meetingPartService.joinMeeting(req, "user-uuid"));
    }

    @Test
    void testJoinMeeting_AlreadyJoined() {
        MeetingParticipantRequest req = new MeetingParticipantRequest();
        req.setMeetingUuid("meeting-uuid");
        when(meetingMapper.findByUuid(anyString())).thenReturn(mockMeeting);
        when(userMapper.findByUuid(anyString())).thenReturn(mockUser);
        when(meetingParticipantMapper.existsByMeetingIdAndUserId(anyLong(), anyLong())).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> meetingPartService.joinMeeting(req, "user-uuid"));
    }

    @Test
    void testCancelParticipation_Normal() {
        when(meetingParticipantMapper.findByUuid(anyString())).thenReturn(mockPart);
        when(userMapper.findByUuid(anyString())).thenReturn(mockUser);
        when(meetingParticipantMapper.updateStatusAndSoftDeleteByUuid(anyString(), anyInt(), anyString())).thenReturn(1);
        assertDoesNotThrow(() -> meetingPartService.cancelParticipation("part-uuid", "user-uuid"));
    }

    @Test
    void testCancelParticipation_NotExist() {
        when(meetingParticipantMapper.findByUuid(anyString())).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> meetingPartService.cancelParticipation("not-exist", "user-uuid"));
    }

    @Test
    void testCancelParticipation_NotOwner() {
        when(meetingParticipantMapper.findByUuid(anyString())).thenReturn(mockPart);
        User other = new User(
            999L, mockUser.getUuid(), mockUser.getName(), mockUser.getEmail(), mockUser.getPassword(),
            mockUser.getPhoneNumber(), mockUser.getAddress(), mockUser.getAvatarUrl(), mockUser.getGender(),
            mockUser.getCompanyId(), mockUser.getRole(), mockUser.getStatus(), mockUser.getDescription(),
            mockUser.getPostCount(), mockUser.getLessonCount(), mockUser.getMeetingCount(),
            mockUser.getCreatedAt(), mockUser.getUpdatedAt()
        );
        when(userMapper.findByUuid(anyString())).thenReturn(other);
        assertThrows(IllegalArgumentException.class, () -> meetingPartService.cancelParticipation("part-uuid", "other-uuid"));
    }

    @Test
    void testCancelParticipation_UpdateFailed() {
        when(meetingParticipantMapper.findByUuid(anyString())).thenReturn(mockPart);
        when(userMapper.findByUuid(anyString())).thenReturn(mockUser);
        when(meetingParticipantMapper.updateStatusAndSoftDeleteByUuid(anyString(), anyInt(), anyString())).thenReturn(0);
        assertThrows(RuntimeException.class, () -> meetingPartService.cancelParticipation("part-uuid", "user-uuid"));
    }
}
