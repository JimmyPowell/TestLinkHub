package tech.cspioneer.backend.service;

import tech.cspioneer.backend.entity.dto.request.NewsAuditReviewRequest;
import tech.cspioneer.backend.entity.dto.request.NewsQueryRequest;
import tech.cspioneer.backend.entity.dto.request.NewsUploadRequest;
import tech.cspioneer.backend.entity.dto.request.NewsUpdateRequest;
import tech.cspioneer.backend.entity.dto.response.*;

import java.util.List;

public interface NewsService {
    int uploadNews(NewsUploadRequest request);
    int updateNews(String uuid, NewsUpdateRequest request);
    int deleteNews(String uuid,String userUuid,String identity);
    NewsDetailResponse getNewsDetail(String uuid,String userUuid,String identity);
    List<NewsListResponse> getNewsList(NewsQueryRequest request);
    List<NewsListResponse> getAllNews(Integer page, Integer pageSize, String userUuid, String identity);

    List<NewsAuditListResponse> getNewsAuditList(int page, int pageSize, String adminUuid);

    NewsAuditDetailResponse getNewsAuditDetail(String uuid);

    void auditNews(String uuid, String adminUuid, NewsAuditReviewRequest newsAuditReviewRequest);

    List<NewsHistoryResponse> getNewsAuditHistoryList(String uuid, String userUuid, String identity);

}
