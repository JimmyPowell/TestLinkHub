package tech.cspioneer.backend.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.cspioneer.backend.entity.*;
import tech.cspioneer.backend.entity.dto.request.MeetingCreateRequest;
import tech.cspioneer.backend.entity.dto.request.MeetingReviewRequest;
import tech.cspioneer.backend.entity.dto.request.MeetingUpdateRequest;
import tech.cspioneer.backend.entity.dto.response.MeetingVersionWithMeetingUuidResponse;
import tech.cspioneer.backend.entity.dto.response.RootReviewResponse;
import tech.cspioneer.backend.exception.ResourceNotFoundException;
import tech.cspioneer.backend.mapper.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingServicelmplTest {
    @Mock
    private CompanyMapper companyMapper;
    @Mock
    private MeetingMapper meetingMapper;
    @Mock
    private MeetingVersionMapper meetingVersionMapper;
    @Mock
    private AuditHistoryMapper auditHistoryMapper;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private MeetingServicelmpl meetingService;

    private Company mockCompany;
    private Meeting mockMeeting;
    private MeetingVersion mockVersion;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockCompany = Company.builder()
                .id(1L).uuid("company-uuid").name("公司").email("c@c.com").password("123")
                .phoneNumber("123456").address("地址").avatarUrl("avatar")
                .companyCode("code").status(null).description("desc")
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        mockMeeting = Meeting.builder()
                .id(2L).uuid("meeting-uuid").creatorId(1L).status("draft")
                .currentVersionId(3L).pendingVersionId(4L)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .isDeleted(0)
                .build();
        mockVersion = MeetingVersion.builder()
                .id(3L).uuid("version-uuid").meetingId(2L).version(1)
                .name("会议v1").description("desc").coverImageUrl("img")
                .startTime(LocalDateTime.now()).endTime(LocalDateTime.now())
                .status("pending_review").editorId(1L).createdAt(LocalDateTime.now())
                .isDeleted(0)
                .build();
        mockUser = User.builder()
                .id(1L).uuid("user-uuid").name("用户").build();
    }

    @Test
    void testCreateMeetingWithVersion_Normal() {
        when(companyMapper.findByUuid(anyString())).thenReturn(mockCompany);
        doNothing().when(meetingMapper).insert(any(Meeting.class));
        doNothing().when(meetingVersionMapper).insert(any(MeetingVersion.class));
        doNothing().when(meetingMapper).update(any(Meeting.class));
        MeetingCreateRequest req = new MeetingCreateRequest();
        req.setName("会议"); req.setDescription("desc"); req.setStartTime("2024-01-01T00:00:00"); req.setEndTime("2024-01-01T01:00:00"); req.setImageUrl("img");
        assertDoesNotThrow(() -> meetingService.createMeetingWithVersion(req, "company-uuid"));
    }

    @Test
    void testCreateMeetingWithVersion_CompanyNotExist() {
        when(companyMapper.findByUuid(anyString())).thenReturn(null);
        MeetingCreateRequest req = new MeetingCreateRequest();
        assertThrows(ResourceNotFoundException.class, () -> meetingService.createMeetingWithVersion(req, "not-exist"));
    }

    @Test
    void testUpdateMeetingWithVersion_Normal() {
        when(companyMapper.findByUuid(anyString())).thenReturn(mockCompany);
        when(meetingMapper.findByUuid(anyString())).thenReturn(mockMeeting);
        when(meetingVersionMapper.findMaxVersionByMeetingId(anyLong())).thenReturn(1);
        doNothing().when(meetingVersionMapper).insert(any(MeetingVersion.class));
        doNothing().when(meetingMapper).update(any(Meeting.class));
        MeetingUpdateRequest req = new MeetingUpdateRequest();
        req.setMeetingUuid("meeting-uuid"); req.setName("会议"); req.setDescription("desc"); req.setStartTime("2024-01-01T00:00:00"); req.setEndTime("2024-01-01T01:00:00"); req.setImageUrl("img");
        assertDoesNotThrow(() -> meetingService.updateMeetingWithVersion(req, "company-uuid"));
    }

    @Test
    void testUpdateMeetingWithVersion_MeetingNotExist() {
        when(companyMapper.findByUuid(anyString())).thenReturn(mockCompany);
        when(meetingMapper.findByUuid(anyString())).thenReturn(null);
        MeetingUpdateRequest req = new MeetingUpdateRequest();
        req.setMeetingUuid("not-exist");
        assertThrows(ResourceNotFoundException.class, () -> meetingService.updateMeetingWithVersion(req, "company-uuid"));
    }

    @Test
    void testCreateMeeting_Admin() {
        when(userMapper.findByUuid(anyString())).thenReturn(mockUser);
        doNothing().when(meetingMapper).insert(any(Meeting.class));
        doNothing().when(meetingVersionMapper).insert(any(MeetingVersion.class));
        doNothing().when(meetingMapper).update(any(Meeting.class));
        MeetingCreateRequest req = new MeetingCreateRequest();
        req.setName("会议"); req.setDescription("desc"); req.setStartTime("2024-01-01T00:00:00"); req.setEndTime("2024-01-01T01:00:00"); req.setImageUrl("img");
        assertDoesNotThrow(() -> meetingService.createMeeting(req, "user-uuid"));
    }

    @Test
    void testUpdateMeeting_Admin() {
        when(userMapper.findByUuid(anyString())).thenReturn(mockUser);
        when(meetingMapper.findByUuid(anyString())).thenReturn(mockMeeting);
        when(meetingVersionMapper.findMaxVersionByMeetingId(anyLong())).thenReturn(1);
        doNothing().when(meetingVersionMapper).insert(any(MeetingVersion.class));
        doNothing().when(meetingMapper).update(any(Meeting.class));
        MeetingUpdateRequest req = new MeetingUpdateRequest();
        req.setMeetingUuid("meeting-uuid"); req.setName("会议"); req.setDescription("desc"); req.setStartTime("2024-01-01T00:00:00"); req.setEndTime("2024-01-01T01:00:00"); req.setImageUrl("img");
        assertDoesNotThrow(() -> meetingService.updateMeeting(req, "user-uuid"));
    }

    @Test
    void testDeleteMeeting_Normal() {
        when(meetingMapper.findByUuid(anyString())).thenReturn(mockMeeting);
        doNothing().when(meetingMapper).update(any(Meeting.class));
        doNothing().when(meetingVersionMapper).softDeleteByMeetingId(anyLong());
        assertDoesNotThrow(() -> meetingService.deleteMeeting("meeting-uuid"));
    }

    @Test
    void testDeleteMeeting_NotExist() {
        when(meetingMapper.findByUuid(anyString())).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> meetingService.deleteMeeting("not-exist"));
    }

    @Test
    void testReviewMeetingCreate_Normal() {
        when(meetingVersionMapper.findByUuid(anyString())).thenReturn(mockVersion);
        when(userMapper.findByUuid(anyString())).thenReturn(mockUser);
        doNothing().when(auditHistoryMapper).insertHistory(anyLong(), anyLong(), anyString(), anyString(), any());
        doNothing().when(meetingVersionMapper).updateStatus(anyLong(), anyString());
        when(meetingMapper.findById(anyLong())).thenReturn(mockMeeting);
        doNothing().when(meetingMapper).update(any(Meeting.class));
        MeetingReviewRequest req = MeetingReviewRequest.builder().meetingVersionUuid("version-uuid").auditStatus("approved").comments("ok").build();
        assertDoesNotThrow(() -> meetingService.reviewMeetingCreate(req, "user-uuid"));
    }

    @Test
    void testReviewMeetingCreate_VersionNotExist() {
        when(meetingVersionMapper.findByUuid(anyString())).thenReturn(null);
        MeetingReviewRequest req = MeetingReviewRequest.builder().meetingVersionUuid("not-exist").build();
        assertThrows(ResourceNotFoundException.class, () -> meetingService.reviewMeetingCreate(req, "user-uuid"));
    }

    @Test
    void testReviewMeetingCreate_UserNotExist() {
        when(meetingVersionMapper.findByUuid(anyString())).thenReturn(mockVersion);
        when(userMapper.findByUuid(anyString())).thenReturn(null);
        MeetingReviewRequest req = MeetingReviewRequest.builder().meetingVersionUuid("version-uuid").build();
        assertThrows(ResourceNotFoundException.class, () -> meetingService.reviewMeetingCreate(req, "not-exist"));
    }

    @Test
    void testReviewMeetingCreate_InvalidStatus() {
        when(meetingVersionMapper.findByUuid(anyString())).thenReturn(mockVersion);
        when(userMapper.findByUuid(anyString())).thenReturn(mockUser);
        MeetingReviewRequest req = MeetingReviewRequest.builder().meetingVersionUuid("version-uuid").auditStatus("invalid").build();
        assertThrows(IllegalArgumentException.class, () -> meetingService.reviewMeetingCreate(req, "user-uuid"));
    }

    @Test
    void testGetPendingReviewList_Normal() {
        List<MeetingVersion> versions = List.of(mockVersion);
        when(meetingVersionMapper.findPendingList(anyInt(), anyInt(), anyString(), nullable(String.class))).thenReturn(versions);
        when(meetingMapper.findById(anyLong())).thenReturn(mockMeeting);
        when(companyMapper.findById(anyLong())).thenReturn(mockCompany);
        List<RootReviewResponse> result = meetingService.getPendingReviewList(1, 10, "pending_review", null);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetMeetingVersionDetails_Normal() {
        when(meetingVersionMapper.findByUuid(anyString())).thenReturn(mockVersion);
        when(meetingMapper.findById(anyLong())).thenReturn(mockMeeting);
        when(companyMapper.findById(anyLong())).thenReturn(mockCompany);
        RootReviewResponse resp = meetingService.getMeetingVersionDetails("version-uuid");
        assertNotNull(resp);
    }

    @Test
    void testGetMeetingVersionDetails_NotExist() {
        when(meetingVersionMapper.findByUuid(anyString())).thenReturn(null);
        assertThrows(RuntimeException.class, () -> meetingService.getMeetingVersionDetails("not-exist"));
    }

    @Test
    void testGetMeetingDetails_Normal() {
        when(meetingMapper.findByUuid(anyString())).thenReturn(mockMeeting);
        when(meetingVersionMapper.findById(anyLong())).thenReturn(mockVersion);
        when(meetingMapper.findById(anyLong())).thenReturn(mockMeeting);
        when(companyMapper.findById(anyLong())).thenReturn(mockCompany);
        RootReviewResponse resp = meetingService.getMeetingDetails("meeting-uuid");
        assertNotNull(resp);
    }

    @Test
    void testGetMeetingDetails_NotExist() {
        when(meetingMapper.findByUuid(anyString())).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> meetingService.getMeetingDetails("not-exist"));
    }

    @Test
    void testGetMeetingDetails_NoCurrentVersion() {
        Meeting meeting = new Meeting(
            mockMeeting.getId(), mockMeeting.getUuid(), mockMeeting.getCreatorId(), mockMeeting.getStatus(),
            null, // currentVersionId 设为null
            mockMeeting.getPendingVersionId(),
            mockMeeting.getCreatedAt(), mockMeeting.getUpdatedAt(),
            mockMeeting.getIsDeleted()
        );
        when(meetingMapper.findByUuid(anyString())).thenReturn(meeting);
        assertThrows(ResourceNotFoundException.class, () -> meetingService.getMeetingDetails("meeting-uuid"));
    }

    @Test
    void testGetMeetingDetails_VersionNotExist() {
        when(meetingMapper.findByUuid(anyString())).thenReturn(mockMeeting);
        when(meetingVersionMapper.findById(anyLong())).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> meetingService.getMeetingDetails("meeting-uuid"));
    }

    @Test
    void testGetPublishedMeetings_Normal() {
        when(meetingMapper.findPublishedMeetings()).thenReturn(List.of(mockMeeting));
        when(meetingVersionMapper.findVersionsByIds(anyList())).thenReturn(List.of(mockVersion));
        when(meetingMapper.findById(anyLong())).thenReturn(mockMeeting);
        when(companyMapper.findById(anyLong())).thenReturn(mockCompany);
        List<RootReviewResponse> result = meetingService.getPublishedMeetings(1, 10);
        assertNotNull(result);
    }

    @Test
    void testGetPublishedMeetings_Empty() {
        when(meetingMapper.findPublishedMeetings()).thenReturn(Collections.emptyList());
        List<RootReviewResponse> result = meetingService.getPublishedMeetings(1, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetMeetingVersionsByCreator_Normal() {
        when(companyMapper.findByUuid(anyString())).thenReturn(mockCompany);
        List<MeetingVersionWithMeetingUuidResponse> list = new ArrayList<>();
        when(meetingVersionMapper.findMeetingVersionsByCreator(anyLong(), any(), any(), any(), anyInt(), anyInt())).thenReturn(list);
        List<MeetingVersionWithMeetingUuidResponse> result = meetingService.getMeetingVersionsByCreator("company-uuid", null, null, null, 1, 10);
        assertNotNull(result);
    }

    @Test
    void testGetMeetingVersionsByCreator_CompanyNotExist() {
        when(companyMapper.findByUuid(anyString())).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> meetingService.getMeetingVersionsByCreator("not-exist", null, null, null, 1, 10));
    }

    @Test
    void testGetMeetingVersionDetail_Normal() {
        when(meetingVersionMapper.findByUuid(anyString())).thenReturn(mockVersion);
        when(meetingMapper.findById(anyLong())).thenReturn(mockMeeting);
        when(companyMapper.findByUuid(anyString())).thenReturn(mockCompany);
        MeetingVersion result = meetingService.getMeetingVersionDetail("version-uuid", "company-uuid");
        assertNotNull(result);
    }

    @Test
    void testGetMeetingVersionDetail_VersionNotExist() {
        when(meetingVersionMapper.findByUuid(anyString())).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> meetingService.getMeetingVersionDetail("not-exist", "company-uuid"));
    }

    @Test
    void testGetMeetingVersionDetail_MeetingNotExist() {
        when(meetingVersionMapper.findByUuid(anyString())).thenReturn(mockVersion);
        when(meetingMapper.findById(anyLong())).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> meetingService.getMeetingVersionDetail("version-uuid", "company-uuid"));
    }

    @Test
    void testGetMeetingVersionDetail_UserNotExist() {
        when(meetingVersionMapper.findByUuid(anyString())).thenReturn(mockVersion);
        when(meetingMapper.findById(anyLong())).thenReturn(mockMeeting);
        when(companyMapper.findByUuid(anyString())).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> meetingService.getMeetingVersionDetail("version-uuid", "not-exist"));
    }

    @Test
    void testGetMeetingVersionDetail_NoPermission() {
        when(meetingVersionMapper.findByUuid(anyString())).thenReturn(mockVersion);
        when(meetingMapper.findById(anyLong())).thenReturn(mockMeeting);
        Company other = Company.builder().id(999L).build();
        when(companyMapper.findByUuid(anyString())).thenReturn(other);
        assertThrows(ResourceNotFoundException.class, () -> meetingService.getMeetingVersionDetail("version-uuid", "company-uuid"));
    }

    @Test
    void testSearchMeetingVersionsByName_Normal() {
        List<MeetingVersion> versions = List.of(mockVersion);
        when(meetingVersionMapper.searchByName(anyString(), anyInt(), anyInt())).thenReturn(versions);
        when(meetingMapper.findById(anyLong())).thenReturn(mockMeeting);
        when(companyMapper.findById(anyLong())).thenReturn(mockCompany);
        List<RootReviewResponse> result = meetingService.searchMeetingVersionsByName("会议", 1, 10);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testSearchMeetingVersionsByStartTimeRange_Normal() {
        List<MeetingVersion> versions = List.of(mockVersion);
        when(meetingVersionMapper.searchByStartTimeRange(any(), any(), anyInt(), anyInt())).thenReturn(versions);
        when(meetingMapper.findById(anyLong())).thenReturn(mockMeeting);
        when(companyMapper.findById(anyLong())).thenReturn(mockCompany);
        List<RootReviewResponse> result = meetingService.searchMeetingVersionsByStartTimeRange(LocalDateTime.now(), LocalDateTime.now(), 1, 10);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testSearchMeetingVersionsByUuid_Normal() {
        List<MeetingVersion> versions = List.of(mockVersion);
        when(meetingVersionMapper.searchByUuid(anyString(), anyInt(), anyInt())).thenReturn(versions);
        when(meetingMapper.findById(anyLong())).thenReturn(mockMeeting);
        when(companyMapper.findById(anyLong())).thenReturn(mockCompany);
        List<RootReviewResponse> result = meetingService.searchMeetingVersionsByUuid("uuid", 1, 10);
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
