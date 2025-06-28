package tech.cspioneer.backend.service.impl;

import io.netty.util.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.cspioneer.backend.entity.*;
import tech.cspioneer.backend.entity.dto.request.NewsAuditReviewRequest;
import tech.cspioneer.backend.entity.dto.request.NewsQueryRequest;
import tech.cspioneer.backend.entity.dto.request.NewsUploadRequest;
import tech.cspioneer.backend.entity.dto.request.NewsUpdateRequest;
import tech.cspioneer.backend.entity.dto.response.*;
import tech.cspioneer.backend.entity.enums.*;
import tech.cspioneer.backend.entity.query.NewsListQuery;
import tech.cspioneer.backend.exception.NewsServiceException;
import tech.cspioneer.backend.mapper.*;
import tech.cspioneer.backend.service.NewsService;
import tech.cspioneer.backend.service.NotificationService;
import tech.cspioneer.backend.utils.CopyTools;
import tech.cspioneer.backend.utils.UuidUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {
    @Autowired
    private NewsMapper newsMapper;
    @Autowired
    private NewsContentMapper newsContentMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CompanyMapper companyMapper;
    @Autowired
    private NewsAuditHistoryMapper newsAuditHistoryMapper;
    @Autowired
    private NotificationService notificationService;

    @Override
    public int uploadNews(NewsUploadRequest request) {
        News news = new News();
        news.setUuid(UuidUtils.randomUuid());
        news.setIsDeleted(0);
        Company checkCompany = companyMapper.findByUuid(request.getCompanyUuid());
        news.setCompanyId(checkCompany.getId());
        news.setVisible(request.getVisible());
        if (request.getIdentity().equals("COMPANY")){
            Company company = companyMapper.findByUuid(request.getUserUUid());
            news.setCompanyId(company.getId());
            news.setStatus(NewsStatus.pending);
            newsMapper.insert(news);
            NewsContent newsContent = new NewsContent();
            newsContent.setUuid(UuidUtils.randomUuid());
            newsContent.setTitle(request.getTitle());
            newsContent.setNewsId(news.getId());
            newsContent.setCoverImageUrl(request.getCoverImageUrl());
            newsContent.setSummary(request.getSummary());
            newsContent.setIsDeleted(0);
            newsContent.setResourceUrl(request.getResourceUrl());
            newsContent.setVersion(1);
            newsContent.setStatus(NewsContentStatus.pending);
            newsContentMapper.insert(newsContent);
            news.setPendingContentId(newsContent.getId());
            newsMapper.update(news);
        }else if (request.getIdentity().equals("ADMIN")){
            User admin = userMapper.findByUuid(request.getUserUUid());
            news.setCompanyId(admin.getId());
            news.setStatus(NewsStatus.published);
            newsMapper.insert(news);
            NewsContent newsContent = new NewsContent();
            newsContent.setUuid(UuidUtils.randomUuid());
            newsContent.setTitle(request.getTitle());
            newsContent.setNewsId(news.getId());
            newsContent.setCoverImageUrl(request.getCoverImageUrl());
            newsContent.setSummary(request.getSummary());
            newsContent.setIsDeleted(0);
            newsContent.setResourceUrl(request.getResourceUrl());
            newsContent.setVersion(1);
            newsContent.setStatus(NewsContentStatus.published);
            newsContentMapper.insert(newsContent);
            news.setPendingContentId(newsContent.getId());
            newsMapper.update(news);
            NewsAuditHistory newsAuditHistory = new NewsAuditHistory();
            newsAuditHistory.setAuditStatus(NewsAuditHistoryStatus.rejected);
            newsAuditHistory.setNewsContentId(newsContent.getId());
            newsAuditHistory.setIsDeleted(0);
            newsAuditHistory.setComments("管理员自动通过");
            newsAuditHistory.setAuditorId(admin.getId());
            newsAuditHistoryMapper.insert(newsAuditHistory);
        }else {
            return -1;
        }
        return 0;
    }

    @Override
    public int updateNews(String uuid, NewsUpdateRequest request) {
        News news = newsMapper.findByUuid(uuid);
        if (news == null){
            throw new NewsServiceException("编辑新闻不存在");
        }
        if (request.getIdentity().equals("COMPANY")){
            Company company = companyMapper.findByUuid(request.getUserUUid());
            if (!company.getId().equals(news.getCompanyId())){
                throw new NewsServiceException("无权限修改此新闻");
            }
            NewsContent newsContent = new NewsContent();
            newsContent.setStatus(NewsContentStatus.pending);
            newsContent.setUuid(UuidUtils.randomUuid());
            newsContent.setTitle(request.getTitle());
            newsContent.setCoverImageUrl(request.getCoverImageUrl());
            newsContent.setSummary(request.getSummary());
            newsContent.setIsDeleted(0);
            newsContent.setResourceUrl(request.getResourceUrl());
            newsContent.setVersion(newsContentMapper.getMaxVersionByNewsId(news.getId())+1);
            newsContentMapper.insert(newsContent);
            news.setPendingContentId(newsContent.getId());
            news.setStatus(NewsStatus.pending);
            newsMapper.update(news);
        }else if (request.getIdentity().equals("ADMIN")){
            User admin = userMapper.findByUuid(request.getUserUUid());
            NewsContent newsContent = new NewsContent();
            newsContent.setStatus(NewsContentStatus.published);
            newsContent.setUuid(UuidUtils.randomUuid());
            newsContent.setTitle(request.getTitle());
            newsContent.setCoverImageUrl(request.getCoverImageUrl());
            newsContent.setSummary(request.getSummary());
            newsContent.setIsDeleted(0);
            newsContent.setResourceUrl(request.getResourceUrl());
            newsContent.setVersion(newsContentMapper.getMaxVersionByNewsId(news.getId())+1);
            newsContentMapper.insert(newsContent);
            news.setCurrentContentId(newsContent.getId());
            news.setStatus(NewsStatus.published);
            newsMapper.update(news);
            NewsAuditHistory newsAuditHistory = new NewsAuditHistory();
            newsAuditHistory.setAuditStatus(NewsAuditHistoryStatus.rejected);
            newsAuditHistory.setNewsContentId(newsContent.getId());
            newsAuditHistory.setIsDeleted(0);
            newsAuditHistory.setComments("管理员自动通过");
            newsAuditHistory.setAuditorId(admin.getId());
            newsAuditHistoryMapper.insert(newsAuditHistory);
        }else {
            return -1;
        }
        return 0;
    }

    @Override
    public int deleteNews(String uuid, String userUuid, String identity) {
        News news = newsMapper.findByUuid(uuid);
        if (news == null) {
            throw new NewsServiceException("新闻不存在");
        }
        if (identity.equals("ADMIN")){
            User admin = userMapper.findByUuid(userUuid);
        }else if (identity.equals("COMPANY")){
            Company company = companyMapper.findByUuid(uuid);
            notificationService.sendSystemNotificationToUser(company.getId(),"您的新闻已被删除","您的新闻以及被管理员删除", RelatedObjectType.COMPANY, company.getId());
        }else {
            return -1;
        }
        newsMapper.deleteById(news.getId());
        return 0;
    }

    @Override
    public NewsDetailResponse getNewsDetail(String uuid, String userUuid, String identity) {
        News news = newsMapper.findByUuid(uuid);
        NewsContent newsContent = newsContentMapper.findById(news.getCurrentContentId());
        NewsDetailResponse newsDetailResponse = CopyTools.copy(newsContent, NewsDetailResponse.class);
        newsDetailResponse.setContentCreatedAt(newsContent.getCreatedAt());
        newsDetailResponse.setCompanyId(news.getCompanyId());
        switch (identity){
            case "ADMIN" -> {
                return newsDetailResponse;
            }
            case "COMPANY" -> {
                Company company = companyMapper.findByUuid(userUuid);
                if (company.getId().equals(news.getCompanyId())||news.getVisible() == 1){
                    return newsDetailResponse;
                }else {
                    throw new NewsServiceException("无查看权限");
                }
            }
            case "USER" -> {
                User user = userMapper.findByUuid(userUuid);
                Company company = companyMapper.findByUuid(uuid);
                if (company.getId().equals(news.getCompanyId())||news.getVisible() == 1){
                    return newsDetailResponse;
                }else {
                    throw new NewsServiceException("无查看权限");
                }
            }
            default -> throw new NewsServiceException("登陆状态异常");
        }
    }


    @Override
    public List<NewsListResponse> getNewsList(NewsQueryRequest request) {
        NewsListQuery newsListQuery = CopyTools.copy(request,NewsListQuery.class);
        List<NewsListResponse> newsListResponses;
        switch (request.getIdentity()) {
            case "ADMIN" -> newsListResponses = newsMapper.findNewsList(newsListQuery);
            case "COMPANY" -> {
                newsListQuery.setCompanyUuid(request.getUserUuid());
                newsListResponses = newsMapper.findNewsList(newsListQuery);
            }
            case "USER" -> {
                User user = userMapper.findByUuid(request.getUserUuid());
                Company company = companyMapper.findById(user.getId());
                newsListQuery.setCompanyUuid(company.getUuid());
                newsListResponses = newsMapper.findNewsList(newsListQuery);
            }
            default -> throw new NewsServiceException("登陆状态异常");
        }
        return newsListResponses;
    }


    @Override
    public List<NewsListResponse> getAllNews(Integer page, Integer pageSize, String userUuid, String identity) {
        List<News> newsList;
        Long companyId = null;
        
        if (identity.equals("ADMIN")) {
            // 管理员可以查看所有新闻
            int offset = (page - 1) * pageSize;
            newsList = newsMapper.findByCompanyIdWithPagination(null, offset, pageSize);
        } else if (identity.equals("COMPANY")) {
            // 公司只能查看自己的新闻
            Company company = companyMapper.findByUuid(userUuid);
            companyId = company.getId();
            int offset = (page - 1) * pageSize;
            newsList = newsMapper.findByCompanyIdWithPagination(companyId, offset, pageSize);
        } else {
            throw new NewsServiceException("无效的用户身份");
        }
        return newsList.stream().map(news -> {
            NewsListResponse response = new NewsListResponse();
            response.setUuid(news.getUuid());
            response.setCompanyId(news.getCompanyId());
            response.setCreatedAt(news.getCreatedAt());
            // 获取新闻内容摘要
            NewsContent content = newsContentMapper.findById(news.getCurrentContentId());
            if (content != null) {
                response.setTitle(content.getTitle());
                response.setSummary(content.getSummary());
                response.setCoverImageUrl(content.getCoverImageUrl());
            }
            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public List<NewsAuditListResponse> getNewsAuditList(int page, int pageSize, String adminUuid) {
        return newsMapper.findPendingNewsList(page,pageSize);
    }

    @Override
    public NewsAuditDetailResponse getNewsAuditDetail(String uuid) {
        return newsMapper.findPendingNewsDetail(uuid);
    }

    @Override
    public void auditNews(String uuid, String adminUuid, NewsAuditReviewRequest newsAuditReviewRequest) {
        User admin = userMapper.findByUuid(uuid);
        switch (newsAuditReviewRequest.getAuditStatus()){
            case "rejected" ->{
                News news = newsMapper.findByUuid(uuid);
                NewsContent newsContent = newsContentMapper.findById(news.getPendingContentId());
                newsContent.setStatus(NewsContentStatus.archived);
                newsContentMapper.update(newsContent);
                NewsAuditHistory newsAuditHistory = new NewsAuditHistory();
                newsAuditHistory.setAuditStatus(NewsAuditHistoryStatus.rejected);
                newsAuditHistory.setNewsContentId(newsContent.getId());
                newsAuditHistory.setIsDeleted(0);
                newsAuditHistory.setComments(newsAuditReviewRequest.getComment());
                newsAuditHistory.setAuditorId(admin.getId());
                newsAuditHistoryMapper.insert(newsAuditHistory);
                if (news.getCurrentContentId() == null){
                    news.setStatus(NewsStatus.archived);
                    newsMapper.update(news);
                }else{
                    news.setStatus(NewsStatus.published);
                    news.setPendingContentId(null);
                    newsMapper.update(news);
                }
                notificationService.sendSystemNotificationToUser(news.getCompanyId(),"您的新闻已被拒绝",newsAuditReviewRequest.getComment(), RelatedObjectType.COMPANY,news.getCompanyId());
            }
            case "approved" ->{
                News news = newsMapper.findByUuid(uuid);
                NewsContent newsContent = newsContentMapper.findById(news.getPendingContentId());
                newsContent.setStatus(NewsContentStatus.published);
                newsContentMapper.update(newsContent);
                NewsAuditHistory newsAuditHistory = new NewsAuditHistory();
                newsAuditHistory.setAuditStatus(NewsAuditHistoryStatus.approved);
                newsAuditHistory.setNewsContentId(newsContent.getId());
                newsAuditHistory.setIsDeleted(0);
                newsAuditHistory.setComments(newsAuditReviewRequest.getComment());
                newsAuditHistory.setAuditorId(admin.getId());
                newsAuditHistoryMapper.insert(newsAuditHistory);
                news.setStatus(NewsStatus.archived);
                news.setCurrentContentId(news.getPendingContentId());
                news.setPendingContentId(null);
                newsMapper.update(news);
                notificationService.sendSystemNotificationToUser(news.getCompanyId(),"您的新闻已被通过",newsAuditReviewRequest.getComment(), RelatedObjectType.COMPANY,news.getCompanyId());
            }
            default -> throw new NewsServiceException("审核状态有误");
        }
    }

    @Override
    public List<NewsHistoryResponse> getNewsAuditHistoryList(String uuid, String userUuid, String identity) {
        switch (identity){
            case "ADMIN" -> {
                return newsMapper.findNewsHistory(uuid);
            }
            case "COMPANY" -> {
                News news = newsMapper.findByUuid(uuid);
                Company company = companyMapper.findByUuid(userUuid);
                if (news.getCompanyId().equals(company.getId())){
                    return newsMapper.findNewsHistory(uuid);
                }else {
                    throw new NewsServiceException("没有权限");
                }
            }
            default -> throw new NewsServiceException("没有权限");
        }
    }

}
