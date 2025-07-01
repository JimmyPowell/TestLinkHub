package tech.cspioneer.backend.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.cspioneer.backend.entity.*;
import tech.cspioneer.backend.entity.dto.request.LessonSearchRequest;
import tech.cspioneer.backend.entity.dto.response.LessonDetailResponse;
import tech.cspioneer.backend.entity.dto.response.LessonListItemResponse;
import tech.cspioneer.backend.entity.dto.response.LessonListResponse;
import tech.cspioneer.backend.entity.dto.response.LessonSearchResponse;
import tech.cspioneer.backend.exception.LessonServiceException;
import tech.cspioneer.backend.mapper.*;
import tech.cspioneer.backend.service.NotificationService;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LessonServiceImplTest {
    @Mock
    private LessonMapper lessonMapper;
    @Mock
    private LessonVersionMapper lessonVersionMapper;
    @Mock
    private LessonResourceMapper lessonResourceMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private CompanyMapper companyMapper;
    @Mock
    private NotificationService notificationService;
    @Mock
    private LessonAuditHistoryMapper lessonAuditHistoryMapper;

    @InjectMocks
    private LessonServiceImpl lessonService;

    private Map<String, Object> lessonRequestBody;

    @BeforeEach
    void setUp() {
        lessonRequestBody = new HashMap<>();
        lessonRequestBody.put("name", "课程1");
        lessonRequestBody.put("description", "描述");
        lessonRequestBody.put("imageUrl", "img.png");
        lessonRequestBody.put("authorName", "作者");
        lessonRequestBody.put("resourcesType", "video");
        lessonRequestBody.put("resourcesUrls", List.of("url1", "url2"));
        lessonRequestBody.put("resourceNames", List.of("资源1", "资源2"));
        lessonRequestBody.put("sortOrders", List.of(1, 2));
    }

    @Test
    void testUploadLesson_Admin() {
        lessonRequestBody.put("identity", "ADMIN");
        lessonRequestBody.put("userId", "admin-uuid");
        User user = new User();
        user.setId(1L);
        user.setLessonCount(0);
        when(userMapper.findByUuid("admin-uuid")).thenReturn(user);
        doAnswer(invocation -> {
            Lesson lesson = invocation.getArgument(0);
            lesson.setId(1L);
            return 1;
        }).when(lessonMapper).insert(any(Lesson.class));
        when(lessonMapper.selectByUuid(anyString())).thenReturn(new Lesson() {{ setId(1L); }});
        doAnswer(invocation -> {
            LessonVersion v = invocation.getArgument(0);
            v.setId(2L);
            return 1;
        }).when(lessonVersionMapper).insert(any(LessonVersion.class));
        when(lessonVersionMapper.selectByUuid(anyString())).thenReturn(new LessonVersion() {{ setId(2L); }});
        assertEquals(1, lessonService.uploadLesson(lessonRequestBody));
        verify(userMapper).update(any(User.class));
    }

    @Test
    void testUploadLesson_Company() {
        lessonRequestBody.put("identity", "COMPANY");
        lessonRequestBody.put("userId", "company-uuid");
        Company company = new Company();
        company.setId(10L);
        when(companyMapper.findByUuid("company-uuid")).thenReturn(company);
        doAnswer(invocation -> {
            Lesson lesson = invocation.getArgument(0);
            lesson.setId(11L);
            return 1;
        }).when(lessonMapper).insert(any(Lesson.class));
        when(lessonMapper.selectByUuid(anyString())).thenReturn(new Lesson() {{ setId(11L); }});
        doAnswer(invocation -> {
            LessonVersion v = invocation.getArgument(0);
            v.setId(12L);
            return 1;
        }).when(lessonVersionMapper).insert(any(LessonVersion.class));
        when(lessonVersionMapper.selectByUuid(anyString())).thenReturn(new LessonVersion() {{ setId(12L); }});
        assertEquals(1, lessonService.uploadLesson(lessonRequestBody));
    }

    @Test
    void testUploadLesson_InvalidResourceType() {
        lessonRequestBody.put("identity", "ADMIN");
        lessonRequestBody.put("userId", "admin-uuid");
        lessonRequestBody.put("resourcesType", "invalid");
        assertThrows(LessonServiceException.class, () -> lessonService.uploadLesson(lessonRequestBody));
    }

    @Test
    void testUploadLesson_InvalidIdentity() {
        lessonRequestBody.put("identity", "USER");
        lessonRequestBody.put("userId", "user-uuid");
        assertEquals(-1, lessonService.uploadLesson(lessonRequestBody));
    }

    @Test
    void testUpdateLesson_Admin() {
        String uuid = "lesson-uuid";
        lessonRequestBody.put("identity", "ADMIN");
        lessonRequestBody.put("userId", "admin-uuid");
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setCurrentVersionId(2L);
        when(lessonMapper.selectByUuid(uuid)).thenReturn(lesson);
        LessonVersion oldVersion = new LessonVersion();
        oldVersion.setId(2L);
        oldVersion.setVersion(1);
        when(lessonVersionMapper.selectById(2L)).thenReturn(oldVersion);
        when(userMapper.findByUuid("admin-uuid")).thenReturn(new User() {{ setId(3L); }});
        doAnswer(invocation -> {
            LessonVersion v = invocation.getArgument(0);
            v.setId(4L);
            return 1;
        }).when(lessonVersionMapper).insert(any(LessonVersion.class));
        when(lessonVersionMapper.selectByUuid(anyString())).thenReturn(new LessonVersion() {{ setId(4L); }});
        assertEquals(1, lessonService.updateLesson(uuid, lessonRequestBody));
    }

    @Test
    void testUpdateLesson_Company() {
        String uuid = "lesson-uuid";
        lessonRequestBody.put("identity", "COMPANY");
        lessonRequestBody.put("userId", "company-uuid");
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setCurrentVersionId(2L);
        when(lessonMapper.selectByUuid(uuid)).thenReturn(lesson);
        when(companyMapper.findByUuid("company-uuid")).thenReturn(new Company() {{ setId(5L); }});
        doAnswer(invocation -> {
            LessonVersion v = invocation.getArgument(0);
            v.setId(6L);
            return 1;
        }).when(lessonVersionMapper).insert(any(LessonVersion.class));
        when(lessonVersionMapper.selectByUuid(anyString())).thenReturn(new LessonVersion() {{ setId(6L); }});
        assertEquals(1, lessonService.updateLesson(uuid, lessonRequestBody));
    }

    @Test
    void testUpdateLesson_NotFound() {
        when(lessonMapper.selectByUuid(anyString())).thenReturn(null);
        assertThrows(LessonServiceException.class, () -> lessonService.updateLesson("not-exist", lessonRequestBody));
    }

    @Test
    void testUpdateLesson_InvalidResourceType() {
        String uuid = "lesson-uuid";
        lessonRequestBody.put("identity", "ADMIN");
        lessonRequestBody.put("userId", "admin-uuid");
        lessonRequestBody.put("resourcesType", "invalid");
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setCurrentVersionId(2L);
        when(lessonMapper.selectByUuid(uuid)).thenReturn(lesson);
        assertThrows(LessonServiceException.class, () -> lessonService.updateLesson(uuid, lessonRequestBody));
    }

    @Test
    void testDeleteLesson() {
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        when(lessonMapper.selectByUuid(anyString())).thenReturn(lesson);
        when(lessonVersionMapper.selectAllByLessonId(1L)).thenReturn(List.of(new LessonVersion() {{ setId(2L); }}));
        assertEquals(1, lessonService.deleteLesson(List.of("uuid1")));
        verify(lessonMapper).softDeleteLessons(anyList());
        verify(lessonVersionMapper).softDeleteVersionsByLessonIds(anyList());
        verify(lessonResourceMapper).softDeleteResourcesByVersionIds(anyList());
    }

    @Test
    void testDeleteLesson_NotFound() {
        when(lessonMapper.selectByUuid(anyString())).thenReturn(null);
        assertEquals(0, lessonService.deleteLesson(List.of("not-exist")));
    }

    @Test
    void testGetAllLessons() {
        when(lessonMapper.selectLessonWithCurrentVersion(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(Map.of("name", "课程1", "image_url", "img.png", "description", "desc", "author_name", "作者", "version", 1, "uuid", "uuid1", "status", "active", "updated_at", "2024-01-01")));
        LessonListResponse resp = lessonService.getAllLessons(null, null, null, null, 0, 10);
        assertEquals(1, resp.getTotal());
        assertEquals("课程1", resp.getList().get(0).getName());
    }

    @Test
    void testGetLessonDetail_Admin() {
        String uuid = "lesson-uuid";
        Lesson lesson = new Lesson();
        lesson.setCurrentVersionId(2L);
        lesson.setPublisherId(1L);
        lesson.setStatus("active");
        when(lessonMapper.selectByUuid(anyString())).thenReturn(lesson);
        LessonVersion version = new LessonVersion();
        version.setId(2L);
        version.setName("课程1");
        version.setImageUrl("img.png");
        version.setDescription("desc");
        version.setAuthorName("作者");
        version.setVersion(1);
        when(lessonVersionMapper.selectById(anyLong())).thenReturn(version);
        LessonResources res = new LessonResources();
        res.setName("资源1");
        res.setResourcesType("video");
        res.setResourcesUrl("url1");
        when(lessonResourceMapper.selectByLessonVersionId(anyLong())).thenReturn(new ArrayList<>(List.of(res)));
        LessonDetailResponse resp = lessonService.getLessonDetail(uuid, 0, 10, null, "ADMIN");
        assertEquals("课程1", resp.getName());
        assertEquals(1, resp.getResources().size());
    }

    @Test
    void testGetLessonDetail_Company_PermissionDenied() {
        String uuid = "lesson-uuid";
        Lesson lesson = new Lesson();
        lesson.setPublisherId(1L);
        lesson.setStatus("active");
        when(lessonMapper.selectByUuid(uuid)).thenReturn(lesson);
        Company company = new Company();
        company.setId(2L);
        when(companyMapper.findByUuid("company-uuid")).thenReturn(company);
        assertThrows(LessonServiceException.class, () -> lessonService.getLessonDetail(uuid, 0, 10, "company-uuid", "COMPANY"));
    }

    @Test
    void testApproveLesson_Approved() {
        String uuid = "lesson-uuid";
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setCurrentVersionId(2L);
        lesson.setPendingVersionId(3L);
        when(lessonMapper.selectByUuid(uuid)).thenReturn(lesson);
        LessonVersion pendingVersion = new LessonVersion();
        pendingVersion.setId(3L);
        when(lessonVersionMapper.selectById(3L)).thenReturn(pendingVersion);
        LessonVersion oldVersion = new LessonVersion();
        oldVersion.setId(2L);
        when(lessonVersionMapper.selectById(2L)).thenReturn(oldVersion);
        when(lessonResourceMapper.selectByLessonVersionId(2L)).thenReturn(List.of());
        when(lessonVersionMapper.selectById(3L)).thenReturn(pendingVersion);
        when(lessonVersionMapper.selectById(2L)).thenReturn(oldVersion);
        Map<String, Object> approvalBody = new HashMap<>();
        approvalBody.put("auditStatus", "approved");
        approvalBody.put("auditorId", 1L);
        approvalBody.put("comment", "同意");
        assertEquals(1, lessonService.approveLesson(uuid, approvalBody));
    }

    @Test
    void testApproveLesson_Rejected() {
        String uuid = "lesson-uuid";
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setPendingVersionId(3L);
        when(lessonMapper.selectByUuid(uuid)).thenReturn(lesson);
        LessonVersion pendingVersion = new LessonVersion();
        pendingVersion.setId(3L);
        when(lessonVersionMapper.selectById(3L)).thenReturn(pendingVersion);
        Map<String, Object> approvalBody = new HashMap<>();
        approvalBody.put("auditStatus", "rejected");
        approvalBody.put("auditorId", 1L);
        approvalBody.put("comment", "不同意");
        assertEquals(1, lessonService.approveLesson(uuid, approvalBody));
    }

    @Test
    void testApproveLesson_NotFound() {
        when(lessonMapper.selectByUuid(anyString())).thenReturn(null);
        Map<String, Object> approvalBody = new HashMap<>();
        approvalBody.put("auditStatus", "approved");
        assertEquals(-1, lessonService.approveLesson("not-exist", approvalBody));
    }

    @Test
    void testSearchLesson() {
        LessonSearchRequest req = new LessonSearchRequest();
        req.setKeyWord("test");
        when(lessonMapper.searchLessonWithCurrentVersion(anyString(), anyInt(), anyInt())).thenReturn(List.of(Map.of("name", "课程1", "uuid", "uuid1")));
        LessonSearchResponse resp = lessonService.searchLesson(req);
        assertEquals(1, resp.getTotal());
        assertEquals("课程1", resp.getList().get(0).getName());
    }

    @Test
    void testGetReviewLessonsWithPendingVersion() {
        when(lessonMapper.selectReviewLessons(
            anyInt(), anyInt(), nullable(String.class), nullable(String.class), nullable(String.class)
        )).thenReturn(new ArrayList<>(List.of(Map.of("uuid", "uuid1"))));
        List<Map<String, Object>> result = lessonService.getReviewLessonsWithPendingVersion(10, 0, "pending_review", null, null);
        assertEquals(1, result.size());
    }

    @Test
    void testGetLessonAuditHistoryPage() {
        when(lessonAuditHistoryMapper.selectHistoryPage(any(), any(), any(), anyInt(), anyInt())).thenReturn(new ArrayList<>(List.of(new LessonAuditHistory())));
        when(lessonAuditHistoryMapper.countHistory(any(), any(), any())).thenReturn(1);
        Map<String, Object> result = lessonService.getLessonAuditHistoryPage(null, null, null, 0, 10);
        assertEquals(1, result.get("total"));
    }

    @Test
    void testSoftDeleteLessonAuditHistory() {
        when(lessonAuditHistoryMapper.softDeleteHistoryByUuids(anyList())).thenReturn(1);
        assertEquals(1, lessonService.softDeleteLessonAuditHistory(List.of("uuid1")));
        assertEquals(0, lessonService.softDeleteLessonAuditHistory(Collections.emptyList()));
    }

    @Test
    void testGetPendingReviewLessonsOverview() {
        Company company = new Company();
        company.setId(1L);
        when(companyMapper.findByUuid(anyString())).thenReturn(company);
        when(lessonMapper.selectCompanyPendingReviewLessonsOverview(any(), any(), any(), anyLong(), anyInt(), anyInt())).thenReturn(new ArrayList<>(List.of(Map.of("uuid", "uuid1"))));
        when(lessonMapper.countCompanyPendingReviewLessonsOverview(any(), any(), any(), anyLong())).thenReturn(1);
        Map<String, Object> result = lessonService.getPendingReviewLessonsOverview(null, null, null, "company-uuid", 0, 10);
        assertEquals(1, result.get("total"));
    }

    @Test
    void testGetLessonsByCompany() {
        when(lessonMapper.findLessonsByCompanyUuidWithVersion(anyString(), any(), any(), any(), anyInt(), anyInt())).thenReturn(new ArrayList<>(List.of(Map.of("name", "课程1", "uuid", "uuid1"))));
        when(lessonMapper.countLessonsByCompanyUuid(anyString(), any(), any(), any())).thenReturn(1L);
        LessonListResponse resp = lessonService.getLessonsByCompany("company-uuid", null, null, null, 0, 10);
        assertEquals(1, resp.getTotal());
    }

    @Test
    void testGetLessonDetailForRoot() {
        Lesson lesson = new Lesson();
        lesson.setPendingVersionId(2L);
        when(lessonMapper.selectByUuid(anyString())).thenReturn(lesson);
        LessonVersion version = new LessonVersion();
        version.setId(2L);
        when(lessonVersionMapper.selectById(anyLong())).thenReturn(version);
        when(lessonResourceMapper.selectByLessonVersionId(anyLong())).thenReturn(new ArrayList<>(List.of(new LessonResources())));
        Map<String, Object> result = lessonService.getLessonDetailForRoot("uuid1");
        assertTrue(result.containsKey("lesson"));
        assertTrue(result.containsKey("version"));
        assertTrue(result.containsKey("resources"));
    }

    @Test
    void testGetLessonDetailForRoot_LessonNotFound() {
        when(lessonMapper.selectByUuid(anyString())).thenReturn(null);
        assertThrows(LessonServiceException.class, () -> lessonService.getLessonDetailForRoot("not-exist"));
    }

    @Test
    void testGetLessonDetailForRoot_VersionNotFound() {
        Lesson lesson = new Lesson();
        lesson.setPendingVersionId(2L);
        when(lessonMapper.selectByUuid(anyString())).thenReturn(lesson);
        when(lessonVersionMapper.selectById(2L)).thenReturn(null);
        assertThrows(LessonServiceException.class, () -> lessonService.getLessonDetailForRoot("uuid1"));
    }
}
