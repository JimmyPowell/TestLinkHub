package tech.cspioneer.backend.entity.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class LessonListResponse {
    private Integer total;
    private List<LessonListItemResponse> list;
} 