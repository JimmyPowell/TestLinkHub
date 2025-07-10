package tech.cspioneer.backend.entity.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class LessonDetailResponse {
    private String name;
    private String imageUrl;
    private String description;
    private String authorName;
    private Integer version;
    private String versionDescription;
    private Integer total;
    private List<LessonResourceItemResponse> resources;
} 