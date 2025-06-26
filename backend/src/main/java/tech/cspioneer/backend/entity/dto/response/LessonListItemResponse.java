package tech.cspioneer.backend.entity.dto.response;

import lombok.Data;

@Data
public class LessonListItemResponse {
    private String name;
    private String imageUrl;
    private String description;
    private String authorName;
    private Integer version;
    private String uuid;
} 