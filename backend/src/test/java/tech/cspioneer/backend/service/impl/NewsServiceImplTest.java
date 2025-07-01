package tech.cspioneer.backend.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.cspioneer.backend.entity.*;
import tech.cspioneer.backend.entity.dto.request.*;
import tech.cspioneer.backend.entity.dto.response.*;
import tech.cspioneer.backend.entity.enums.*;
import tech.cspioneer.backend.entity.query.NewsListQuery;
import tech.cspioneer.backend.exception.NewsServiceException;
import tech.cspioneer.backend.mapper.*;
import tech.cspioneer.backend.service.NotificationService;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsServiceImplTest {
    @Mock NewsMapper newsMapper;
    @Mock NewsContentMapper newsContentMapper;
    @Mock UserMapper userMapper;
    @Mock CompanyMapper companyMapper;
    @Mock NewsAuditHistoryMapper newsAuditHistoryMapper;
    @Mock NotificationService notificationService;
    @InjectMocks NewsServiceImpl newsService;

    @BeforeEach
    void setUp() {
        // 可选：重置mock
        Mockito.reset(newsMapper, newsContentMapper, userMapper, companyMapper, newsAuditHistoryMapper, notificationService);
    }

    @Test
    void testUploadNews_Company_Success() {
        NewsUploadRequest req = new NewsUploadRequest();
        req.setIdentity("COMPANY");
        req.setCompanyUuid("c-uuid");
        req.setUserUUid("u-uuid");
        req.setTitle("title");
        req.setCoverImageUrl("img");
        req.setSummary("sum");
        req.setResourceUrl("url");
        req.setVisible(1);
        Company company = new Company(); company.setId(1L);
        when(companyMapper.findByUuid("c-uuid")).thenReturn(company);
        when(companyMapper.findByUuid("u-uuid")).thenReturn(company);
        when(newsMapper.insert(any())).thenReturn(1);
        when(newsContentMapper.insert(any())).thenReturn(1);
        when(newsMapper.update(any())).thenReturn(1);
        int res = newsService.uploadNews(req);
        assertEquals(0, res);
        verify(newsMapper, times(1)).insert(any());
        verify(newsContentMapper, times(1)).insert(any());
        verify(newsMapper, atLeastOnce()).update(any());
    }

    @Test
    void testUploadNews_Admin_Success() {
        NewsUploadRequest req = new NewsUploadRequest();
        req.setIdentity("ADMIN");
        req.setCompanyUuid("c-uuid");
        req.setUserUUid("u-uuid");
        req.setTitle("title");
        req.setCoverImageUrl("img");
        req.setSummary("sum");
        req.setResourceUrl("url");
        req.setVisible(1);
        Company company = new Company(); company.setId(1L);
        when(companyMapper.findByUuid("c-uuid")).thenReturn(company);
        User admin = new User(); admin.setId(2L);
        when(userMapper.findByUuid("u-uuid")).thenReturn(admin);
        when(newsMapper.insert(any())).thenReturn(1);
        when(newsContentMapper.insert(any())).thenReturn(1);
        when(newsMapper.update(any())).thenReturn(1);
        when(newsAuditHistoryMapper.insert(any())).thenReturn(1);
        int res = newsService.uploadNews(req);
        assertEquals(0, res);
        verify(newsAuditHistoryMapper, times(1)).insert(any());
    }

    @Test
    void testUploadNews_UnknownIdentity() {
        NewsUploadRequest req = new NewsUploadRequest();
        req.setIdentity("UNKNOWN");
        req.setCompanyUuid("c-uuid");
        Company company = new Company(); company.setId(1L);
        when(companyMapper.findByUuid("c-uuid")).thenReturn(company);
        int res = newsService.uploadNews(req);
        assertEquals(-1, res);
    }

    @Test
    void testUpdateNews_Company_Success() {
        NewsUpdateRequest req = new NewsUpdateRequest();
        req.setIdentity("COMPANY");
        req.setUserUUid("u-uuid");
        req.setTitle("t"); req.setCoverImageUrl("img"); req.setSummary("s"); req.setResourceUrl("url");
        News news = new News(); news.setId(1L); news.setCompanyId(2L);
        Company company = new Company(); company.setId(2L);
        when(newsMapper.findByUuid(anyString())).thenReturn(news);
        when(companyMapper.findByUuid("u-uuid")).thenReturn(company);
        when(newsContentMapper.getMaxVersionByNewsId(1L)).thenReturn(1);
        when(newsContentMapper.insert(any())).thenReturn(1);
        when(newsMapper.update(any())).thenReturn(1);
        int res = newsService.updateNews("uuid", req);
        assertEquals(0, res);
    }

    @Test
    void testUpdateNews_Admin_Success() {
        NewsUpdateRequest req = new NewsUpdateRequest();
        req.setIdentity("ADMIN");
        req.setUserUUid("u-uuid");
        req.setTitle("t"); req.setCoverImageUrl("img"); req.setSummary("s"); req.setResourceUrl("url");
        News news = new News(); news.setId(1L); news.setCompanyId(2L);
        User admin = new User(); admin.setId(3L);
        when(newsMapper.findByUuid(anyString())).thenReturn(news);
        when(userMapper.findByUuid("u-uuid")).thenReturn(admin);
        when(newsContentMapper.getMaxVersionByNewsId(1L)).thenReturn(1);
        when(newsContentMapper.insert(any())).thenReturn(1);
        when(newsMapper.update(any())).thenReturn(1);
        when(newsAuditHistoryMapper.insert(any())).thenReturn(1);
        int res = newsService.updateNews("uuid", req);
        assertEquals(0, res);
    }

    @Test
    void testUpdateNews_NewsNotFound() {
        when(newsMapper.findByUuid(anyString())).thenReturn(null);
        NewsUpdateRequest req = new NewsUpdateRequest();
        req.setIdentity("COMPANY");
        Exception ex = assertThrows(NewsServiceException.class, () -> newsService.updateNews("uuid", req));
        assertTrue(ex.getMessage().contains("编辑新闻不存在"));
    }

    @Test
    void testUpdateNews_CompanyNoPermission() {
        NewsUpdateRequest req = new NewsUpdateRequest();
        req.setIdentity("COMPANY");
        req.setUserUUid("u-uuid");
        News news = new News(); news.setId(1L); news.setCompanyId(2L);
        Company company = new Company(); company.setId(3L);
        when(newsMapper.findByUuid(anyString())).thenReturn(news);
        when(companyMapper.findByUuid("u-uuid")).thenReturn(company);
        Exception ex = assertThrows(NewsServiceException.class, () -> newsService.updateNews("uuid", req));
        assertTrue(ex.getMessage().contains("无权限修改此新闻"));
    }

    @Test
    void testUpdateNews_AdminNotFound() {
        NewsUpdateRequest req = new NewsUpdateRequest();
        req.setIdentity("ADMIN");
        req.setUserUUid("u-uuid");
        News news = new News(); news.setId(1L);
        when(newsMapper.findByUuid(anyString())).thenReturn(news);
        when(userMapper.findByUuid("u-uuid")).thenReturn(null);
        Exception ex = assertThrows(NewsServiceException.class, () -> newsService.updateNews("uuid", req));
        assertTrue(ex.getMessage().contains("管理员用户不存在"));
    }

    @Test
    void testUpdateNews_UnknownIdentity() {
        NewsUpdateRequest req = new NewsUpdateRequest();
        req.setIdentity("UNKNOWN");
        News news = new News();
        when(newsMapper.findByUuid(anyString())).thenReturn(news);
        int res = newsService.updateNews("uuid", req);
        assertEquals(-1, res);
    }

    @Test
    void testDeleteNews_Admin_Success() {
        News news = new News(); news.setId(1L); news.setUuid("n-uuid");
        User admin = new User(); admin.setName("admin"); admin.setUuid("a-uuid");
        when(newsMapper.findByUuid("n-uuid")).thenReturn(news);
        when(userMapper.findByUuid("a-uuid")).thenReturn(admin);
        when(newsMapper.deleteById(1L)).thenReturn(1);
        int res = newsService.deleteNews("n-uuid", "a-uuid", "ADMIN");
        assertEquals(0, res);
    }

    @Test
    void testDeleteNews_Company_Success() {
        News news = new News(); news.setId(1L); news.setCompanyId(2L);
        Company company = new Company(); company.setId(2L);
        when(newsMapper.findByUuid("n-uuid")).thenReturn(news);
        when(companyMapper.findByUuid("c-uuid")).thenReturn(company);
        when(newsMapper.deleteById(1L)).thenReturn(1);
        int res = newsService.deleteNews("n-uuid", "c-uuid", "COMPANY");
        assertEquals(0, res);
        verify(notificationService, times(1)).sendSystemNotificationToCompany(eq(2L), anyString(), anyString(), any(), eq(2L));
    }

    @Test
    void testDeleteNews_Company_NoPermission() {
        News news = new News(); news.setId(1L); news.setCompanyId(2L);
        Company company = new Company(); company.setId(3L);
        when(newsMapper.findByUuid("n-uuid")).thenReturn(news);
        when(companyMapper.findByUuid("c-uuid")).thenReturn(company);
        Exception ex = assertThrows(NewsServiceException.class, () -> newsService.deleteNews("n-uuid", "c-uuid", "COMPANY"));
        assertTrue(ex.getMessage().contains("无权限删除此新闻"));
    }

    @Test
    void testDeleteNews_NewsNotFound() {
        when(newsMapper.findByUuid(anyString())).thenReturn(null);
        Exception ex = assertThrows(NewsServiceException.class, () -> newsService.deleteNews("uuid", "u", "ADMIN"));
        assertTrue(ex.getMessage().contains("新闻不存在"));
    }

    @Test
    void testDeleteNews_UnknownIdentity() {
        News news = new News(); news.setId(1L);
        when(newsMapper.findByUuid(anyString())).thenReturn(news);
        int res = newsService.deleteNews("uuid", "u", "UNKNOWN");
        assertEquals(-1, res);
    }

    @Test
    void testGetNewsDetail_Admin_Success() {
        News news = new News();
        news.setCurrentContentId(10L);
        news.setUuid("n-uuid");
        news.setCompanyId(1L);
        news.setVisible(1);
        NewsContent content = new NewsContent();
        content.setCreatedAt(LocalDateTime.now());
        content.setPublisherId(1L);
        when(newsMapper.findByUuid("n-uuid")).thenReturn(news);
        when(newsContentMapper.findById(10L)).thenReturn(content);
        NewsDetailResponse resp = newsService.getNewsDetail("n-uuid", "admin-uuid", "ADMIN");
        assertNotNull(resp);
    }

    @Test
    void testGetNewsDetail_Company_Own_Success() {
        News news = new News();
        news.setCurrentContentId(10L);
        news.setUuid("n-uuid");
        news.setCompanyId(1L);
        news.setVisible(0);
        NewsContent content = new NewsContent();
        content.setCreatedAt(LocalDateTime.now());
        content.setPublisherId(1L);
        Company company = new Company(); company.setId(1L);
        when(newsMapper.findByUuid("n-uuid")).thenReturn(news);
        when(newsContentMapper.findById(10L)).thenReturn(content);
        when(companyMapper.findByUuid("c-uuid")).thenReturn(company);
        NewsDetailResponse resp = newsService.getNewsDetail("n-uuid", "c-uuid", "COMPANY");
        assertNotNull(resp);
    }

    @Test
    void testGetNewsDetail_Company_NoPermission() {
        News news = new News();
        news.setCurrentContentId(10L);
        news.setUuid("n-uuid");
        news.setCompanyId(1L);
        news.setVisible(0);
        NewsContent content = new NewsContent();
        content.setCreatedAt(LocalDateTime.now());
        content.setPublisherId(1L);
        Company company = new Company(); company.setId(2L);
        when(newsMapper.findByUuid("n-uuid")).thenReturn(news);
        when(newsContentMapper.findById(10L)).thenReturn(content);
        when(companyMapper.findByUuid("c-uuid")).thenReturn(company);
        assertThrows(NewsServiceException.class, () -> newsService.getNewsDetail("n-uuid", "c-uuid", "COMPANY"));
    }

    @Test
    void testGetNewsDetail_User_Success() {
        News news = new News();
        news.setCurrentContentId(10L);
        news.setUuid("n-uuid");
        news.setCompanyId(1L);
        news.setVisible(1);
        NewsContent content = new NewsContent();
        content.setCreatedAt(LocalDateTime.now());
        content.setPublisherId(1L);
        User user = new User(); user.setCompanyId(1L);
        Company company = new Company(); company.setId(1L);
        when(newsMapper.findByUuid("n-uuid")).thenReturn(news);
        when(newsContentMapper.findById(10L)).thenReturn(content);
        when(userMapper.findByUuid("u-uuid")).thenReturn(user);
        when(companyMapper.findByid(1L)).thenReturn(company);
        NewsDetailResponse resp = newsService.getNewsDetail("n-uuid", "u-uuid", "USER");
        assertNotNull(resp);
    }

    @Test
    void testGetNewsDetail_User_NoPermission() {
        News news = new News();
        news.setCurrentContentId(10L);
        news.setUuid("n-uuid");
        news.setCompanyId(1L);
        news.setVisible(0);
        NewsContent content = new NewsContent();
        content.setCreatedAt(LocalDateTime.now());
        content.setPublisherId(1L);
        User user = new User(); user.setCompanyId(2L);
        Company company = new Company(); company.setId(3L);
        when(newsMapper.findByUuid("n-uuid")).thenReturn(news);
        when(newsContentMapper.findById(10L)).thenReturn(content);
        when(userMapper.findByUuid("u-uuid")).thenReturn(user);
        when(companyMapper.findByid(2L)).thenReturn(company);
        assertThrows(NewsServiceException.class, () -> newsService.getNewsDetail("n-uuid", "u-uuid", "USER"));
    }

    @Test
    void testGetNewsDetail_UnknownIdentity() {
        News news = new News();
        news.setCurrentContentId(10L);
        news.setUuid("n-uuid");
        news.setCompanyId(1L);
        news.setVisible(1);
        NewsContent content = new NewsContent();
        content.setCreatedAt(LocalDateTime.now());
        content.setPublisherId(1L);
        when(newsMapper.findByUuid("n-uuid")).thenReturn(news);
        when(newsContentMapper.findById(10L)).thenReturn(content);
        assertThrows(NewsServiceException.class, () -> newsService.getNewsDetail("n-uuid", "u-uuid", "UNKNOWN"));
    }

    @Test
    void testGetNewsList_Admin() {
        NewsQueryRequest req = new NewsQueryRequest();
        req.setIdentity("ADMIN");
        req.setPage(1); req.setPageSize(10);
        List<NewsListResponse> list = new ArrayList<>();
        when(newsMapper.findNewsList(any(), anyInt())).thenReturn(list);
        List<NewsListResponse> res = newsService.getNewsList(req);
        assertSame(list, res);
    }

    @Test
    void testGetNewsList_Company() {
        NewsQueryRequest req = new NewsQueryRequest();
        req.setIdentity("COMPANY");
        req.setUserUuid("c-uuid");
        req.setPage(1); req.setPageSize(10);
        List<NewsListResponse> list = new ArrayList<>();
        when(newsMapper.findNewsList(any(), anyInt())).thenReturn(list);
        List<NewsListResponse> res = newsService.getNewsList(req);
        assertSame(list, res);
    }

    @Test
    void testGetNewsList_User() {
        NewsQueryRequest req = new NewsQueryRequest();
        req.setIdentity("USER");
        req.setUserUuid("u-uuid");
        req.setPage(1); req.setPageSize(10);
        User user = new User(); user.setCompanyId(1L);
        Company company = new Company(); company.setUuid("c-uuid");
        when(userMapper.findByUuid("u-uuid")).thenReturn(user);
        when(companyMapper.findById(1L)).thenReturn(company);
        when(newsMapper.findNewsList(any(), anyInt())).thenReturn(new ArrayList<>());
        List<NewsListResponse> res = newsService.getNewsList(req);
        assertNotNull(res);
    }

    @Test
    void testGetNewsList_UnknownIdentity() {
        NewsQueryRequest req = new NewsQueryRequest();
        req.setIdentity("UNKNOWN");
        req.setPage(1); req.setPageSize(10);
        assertThrows(NewsServiceException.class, () -> newsService.getNewsList(req));
    }

    @Test
    void testGetAllNews_Admin() {
        News news = new News(); news.setUuid("n-uuid"); news.setCompanyId(1L); news.setCreatedAt(LocalDateTime.now());
        news.setPendingContentId(10L); news.setStatus(NewsStatus.pending);
        NewsContent content = new NewsContent(); content.setTitle("t"); content.setSummary("s"); content.setCoverImageUrl("img"); content.setCreatedAt(LocalDateTime.now()); content.setPublisherId(1L);
        when(newsMapper.findByCompanyIdWithPagination(null, 0, 10)).thenReturn(List.of(news));
        when(newsContentMapper.findById(10L)).thenReturn(content);
        List<NewsListResponse> res = newsService.getAllNews(1, 10, "admin-uuid", "ADMIN");
        assertEquals(1, res.size());
        assertEquals("t", res.get(0).getTitle());
    }

    @Test
    void testGetAllNews_Company() {
        Company company = new Company(); company.setId(1L);
        News news = new News(); news.setUuid("n-uuid"); news.setCompanyId(1L); news.setCreatedAt(LocalDateTime.now());
        news.setPendingContentId(10L); news.setStatus(NewsStatus.pending);
        NewsContent content = new NewsContent(); content.setTitle("t"); content.setSummary("s"); content.setCoverImageUrl("img"); content.setCreatedAt(LocalDateTime.now()); content.setPublisherId(1L);
        when(companyMapper.findByUuid("c-uuid")).thenReturn(company);
        when(newsMapper.findByCompanyIdWithPagination(1L, 0, 10)).thenReturn(List.of(news));
        when(newsContentMapper.findById(10L)).thenReturn(content);
        List<NewsListResponse> res = newsService.getAllNews(1, 10, "c-uuid", "COMPANY");
        assertEquals(1, res.size());
        assertEquals("t", res.get(0).getTitle());
    }

    @Test
    void testGetAllNews_InvalidIdentity() {
        assertThrows(NewsServiceException.class, () -> newsService.getAllNews(1, 10, "u", "UNKNOWN"));
    }

    @Test
    void testGetNewsDetailForAdmin_Success() {
        News news = new News(); news.setPendingContentId(10L); news.setCurrentContentId(11L); news.setCompanyId(1L); news.setUuid("n-uuid");
        NewsContent content = new NewsContent(); content.setCreatedAt(LocalDateTime.now()); content.setPublisherId(1L);
        when(newsMapper.findByUuid("n-uuid")).thenReturn(news);
        when(newsContentMapper.findById(10L)).thenReturn(content);
        NewsDetailResponse resp = newsService.getNewsDetailForAdmin("n-uuid");
        assertNotNull(resp);
    }

    @Test
    void testGetNewsDetailForAdmin_NewsNotFound() {
        when(newsMapper.findByUuid("n-uuid")).thenReturn(null);
        assertThrows(NewsServiceException.class, () -> newsService.getNewsDetailForAdmin("n-uuid"));
    }

    @Test
    void testGetNewsDetailForAdmin_ContentIdNull() {
        News news = new News(); news.setPendingContentId(null); news.setCurrentContentId(null);
        when(newsMapper.findByUuid("n-uuid")).thenReturn(news);
        assertThrows(NewsServiceException.class, () -> newsService.getNewsDetailForAdmin("n-uuid"));
    }

    @Test
    void testGetNewsDetailForAdmin_ContentNotFound() {
        News news = new News(); news.setPendingContentId(10L); news.setCurrentContentId(11L);
        when(newsMapper.findByUuid("n-uuid")).thenReturn(news);
        when(newsContentMapper.findById(10L)).thenReturn(null);
        assertThrows(NewsServiceException.class, () -> newsService.getNewsDetailForAdmin("n-uuid"));
    }

    @Test
    void testGetNewsAuditList() {
        when(newsMapper.findNewsForAuditList(anyString(), anyInt(), anyInt())).thenReturn(new ArrayList<>());
        List<NewsAuditListResponse> res = newsService.getNewsAuditList(1, 10, "pending", "admin-uuid");
        assertNotNull(res);
    }

    @Test
    void testGetNewsAuditDetail_Pending() {
        NewsAuditDetailResponse detail = new NewsAuditDetailResponse();
        detail.setCompanyId(1L); detail.setPublisherId(2L); detail.setResourceUrl(null);
        Company company = new Company(); company.setId(1L); company.setName("c");
        when(newsMapper.findPendingNewsDetail("n-uuid")).thenReturn(detail);
        when(companyMapper.findById(1L)).thenReturn(company);
        when(companyMapper.findById(2L)).thenReturn(company);
        NewsAuditDetailResponse res = newsService.getNewsAuditDetail("n-uuid");
        assertNotNull(res);
        assertEquals("c", res.getCompanyName());
    }

    @Test
    void testGetNewsAuditDetail_Published() {
        when(newsMapper.findPendingNewsDetail("n-uuid")).thenReturn(null);
        NewsAuditDetailResponse detail = new NewsAuditDetailResponse();
        detail.setCompanyId(1L); detail.setPublisherId(2L); detail.setResourceUrl(null);
        Company company = new Company(); company.setId(1L); company.setName("c");
        when(newsMapper.findPublishedNewsDetail("n-uuid")).thenReturn(detail);
        when(companyMapper.findById(1L)).thenReturn(company);
        when(companyMapper.findById(2L)).thenReturn(company);
        NewsAuditDetailResponse res = newsService.getNewsAuditDetail("n-uuid");
        assertNotNull(res);
    }

    @Test
    void testGetNewsAuditDetail_NotFound() {
        when(newsMapper.findPendingNewsDetail("n-uuid")).thenReturn(null);
        when(newsMapper.findPublishedNewsDetail("n-uuid")).thenReturn(null);
        NewsAuditDetailResponse res = newsService.getNewsAuditDetail("n-uuid");
        assertNull(res);
    }

    @Test
    void testAuditNews_Approved() {
        News news = new News(); news.setPendingContentId(10L); news.setId(1L);
        NewsContent content = new NewsContent(); content.setId(10L); content.setPublisherId(1L);
        User admin = new User(); admin.setId(2L);
        NewsAuditReviewRequest req = new NewsAuditReviewRequest(); req.setAuditStatus("approved"); req.setComment("ok");
        when(userMapper.findByUuid("admin-uuid")).thenReturn(admin);
        when(newsMapper.findByUuid("n-uuid")).thenReturn(news);
        when(newsContentMapper.findById(10L)).thenReturn(content);
        when(newsContentMapper.update(any())).thenReturn(1);
        when(newsAuditHistoryMapper.insert(any())).thenReturn(1);
        when(newsMapper.update(any())).thenReturn(1);
        newsService.auditNews("n-uuid", "admin-uuid", req);
        verify(newsContentMapper, times(1)).update(any());
        verify(newsAuditHistoryMapper, times(1)).insert(any());
        verify(newsMapper, times(1)).update(any());
    }

    @Test
    void testAuditNews_Rejected() {
        News news = new News(); news.setPendingContentId(10L); news.setId(1L);
        NewsContent content = new NewsContent(); content.setId(10L); content.setPublisherId(1L);
        User admin = new User(); admin.setId(2L);
        NewsAuditReviewRequest req = new NewsAuditReviewRequest(); req.setAuditStatus("rejected"); req.setComment("fail");
        when(userMapper.findByUuid("admin-uuid")).thenReturn(admin);
        when(newsMapper.findByUuid("n-uuid")).thenReturn(news);
        when(newsContentMapper.findById(10L)).thenReturn(content);
        when(newsContentMapper.update(any())).thenReturn(1);
        when(newsAuditHistoryMapper.insert(any())).thenReturn(1);
        when(newsMapper.update(any())).thenReturn(1);
        newsService.auditNews("n-uuid", "admin-uuid", req);
        verify(newsContentMapper, times(1)).update(any());
        verify(newsAuditHistoryMapper, times(1)).insert(any());
        verify(newsMapper, times(1)).update(any());
    }

    @Test
    void testAuditNews_AdminNotFound() {
        NewsAuditReviewRequest req = new NewsAuditReviewRequest(); req.setAuditStatus("approved");
        when(userMapper.findByUuid("admin-uuid")).thenReturn(null);
        Exception ex = assertThrows(NewsServiceException.class, () -> newsService.auditNews("n-uuid", "admin-uuid", req));
        assertTrue(ex.getMessage().contains("Auditor"));
    }

    @Test
    void testAuditNews_InvalidStatus() {
        NewsAuditReviewRequest req = new NewsAuditReviewRequest(); req.setAuditStatus("other");
        User admin = new User(); admin.setId(2L);
        when(userMapper.findByUuid("admin-uuid")).thenReturn(admin);
        Exception ex = assertThrows(NewsServiceException.class, () -> newsService.auditNews("n-uuid", "admin-uuid", req));
        assertTrue(ex.getMessage().contains("审核状态有误"));
    }

    @Test
    void testGetNewsAuditHistoryList_Admin() {
        when(newsMapper.findNewsHistory("n-uuid")).thenReturn(new ArrayList<>());
        List<NewsHistoryResponse> res = newsService.getNewsAuditHistoryList("n-uuid", "admin-uuid", "ADMIN");
        assertNotNull(res);
    }

    @Test
    void testGetNewsAuditHistoryList_Company_Success() {
        News news = new News(); news.setCompanyId(1L);
        Company company = new Company(); company.setId(1L);
        when(newsMapper.findByUuid("n-uuid")).thenReturn(news);
        when(companyMapper.findByUuid("c-uuid")).thenReturn(company);
        when(newsMapper.findNewsHistory("n-uuid")).thenReturn(new ArrayList<>());
        List<NewsHistoryResponse> res = newsService.getNewsAuditHistoryList("n-uuid", "c-uuid", "COMPANY");
        assertNotNull(res);
    }

    @Test
    void testGetNewsAuditHistoryList_Company_NoPermission() {
        News news = new News(); news.setCompanyId(1L);
        Company company = new Company(); company.setId(2L);
        when(newsMapper.findByUuid("n-uuid")).thenReturn(news);
        when(companyMapper.findByUuid("c-uuid")).thenReturn(company);
        Exception ex = assertThrows(NewsServiceException.class, () -> newsService.getNewsAuditHistoryList("n-uuid", "c-uuid", "COMPANY"));
        assertTrue(ex.getMessage().contains("没有权限"));
    }

    @Test
    void testGetNewsAuditHistoryList_UnknownIdentity() {
        Exception ex = assertThrows(NewsServiceException.class, () -> newsService.getNewsAuditHistoryList("n-uuid", "u", "USER"));
        assertTrue(ex.getMessage().contains("没有权限"));
    }

    @Test
    void testDeleteNewsAsRoot_Success() {
        News news = new News(); news.setId(1L); news.setUuid("n-uuid");
        when(newsMapper.findByUuid("n-uuid")).thenReturn(news);
        when(newsMapper.deleteById(1L)).thenReturn(1);
        newsService.deleteNewsAsRoot("n-uuid");
        verify(newsMapper, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteNewsAsRoot_NotFound() {
        when(newsMapper.findByUuid("n-uuid")).thenReturn(null);
        Exception ex = assertThrows(NewsServiceException.class, () -> newsService.deleteNewsAsRoot("n-uuid"));
        assertTrue(ex.getMessage().contains("要删除的新闻不存在"));
    }
} 