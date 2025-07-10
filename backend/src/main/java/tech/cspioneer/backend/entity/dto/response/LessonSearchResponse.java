package tech.cspioneer.backend.entity.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class LessonSearchResponse {
    private Integer total;
    private List<LessonListItemResponse> list;
} 