package tech.cspioneer.backend.entity.dto.request;

import lombok.Data;

@Data
public class LessonDetailRequest {
    private String uuid;
    private Integer page = 0;
    private Integer size = 10;
} 