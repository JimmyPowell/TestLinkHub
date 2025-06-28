package tech.cspioneer.backend.entity.dto.request;

import lombok.Data;

@Data
public class LessonSearchRequest {
    private String keyWord;
    private Integer page = 0;
    private Integer size = 10;
} 