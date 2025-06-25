package tech.cspioneer.backend.service.impl;

import io.netty.util.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.cspioneer.backend.entity.Company;
import tech.cspioneer.backend.entity.News;
import tech.cspioneer.backend.entity.NewsContent;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.entity.dto.request.NewsUploadRequest;
import tech.cspioneer.backend.entity.dto.request.NewsUpdateRequest;
import tech.cspioneer.backend.entity.dto.response.NewsDetailResponse;
import tech.cspioneer.backend.entity.dto.response.NewsListResponse;
import tech.cspioneer.backend.entity.enums.NewsContentStatus;
import tech.cspioneer.backend.entity.enums.NewsStatus;
import tech.cspioneer.backend.exception.NewsServiceException;
import tech.cspioneer.backend.mapper.CompanyMapper;
import tech.cspioneer.backend.mapper.NewsContentMapper;
import tech.cspioneer.backend.mapper.NewsMapper;
import tech.cspioneer.backend.mapper.UserMapper;
import tech.cspioneer.backend.service.NewsService;
import tech.cspioneer.backend.utils.UuidUtils;

import java.time.LocalDateTime;
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

    @Override
    public int uploadNews(NewsUploadRequest request) {
        News news = new News();
        news.setUuid(UuidUtils.randomUuid());
        news.setIsDeleted(0);
        news.setCompanyId(request.getCompanyId());
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
            news.setPendingContendId(newsContent.getId());
            newsMapper.update(news);
        }else if (request.getIdentity().equals("ADMIN")){
            User user = userMapper.findByUuid(request.getUserUUid());
            news.setCompanyId(user.getId());
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
            news.setPendingContendId(newsContent.getId());
            newsMapper.update(news);
        }else {
            return -1;
        }
        return 0;
    }

    @Override
    public int updateNews(String uuid, NewsUpdateRequest request) {
        return 0;
    }

    @Override
    public int deleteNews(String uuid) {
        return 0;
    }

    @Override
    public NewsDetailResponse getNewsDetail(String uuid) {
        return null;
    }

    @Override
    public List<NewsListResponse> getNewsList(Integer page, Integer pageSize) {
        return null;
    }

    @Override
    public List<NewsListResponse> getAllNews(Integer page, Integer pageSize) {
        return null;
    }
}
