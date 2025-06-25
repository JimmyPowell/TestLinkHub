package tech.cspioneer.backend.service;

import tech.cspioneer.backend.entity.dto.request.NewsUploadRequest;
import tech.cspioneer.backend.entity.dto.request.NewsUpdateRequest;
import tech.cspioneer.backend.entity.dto.response.NewsDetailResponse;
import tech.cspioneer.backend.entity.dto.response.NewsListResponse;

import java.util.List;

public interface NewsService {
    int uploadNews(NewsUploadRequest request);
    int updateNews(String uuid, NewsUpdateRequest request);
    int deleteNews(String uuid);
    NewsDetailResponse getNewsDetail(String uuid);
    List<NewsListResponse> getNewsList(Integer page, Integer pageSize);
    List<NewsListResponse> getAllNews(Integer page, Integer pageSize);
}
