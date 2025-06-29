package tech.cspioneer.backend.entity.dto.response;

import lombok.Data;

@Data
public class LessonResourceItemResponse {
    private String name;
    private String resourcesType;
    private String resourcesUrl;
}
