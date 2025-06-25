package tech.cspioneer.backend.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonResources {
    private Long id;
    private String uuid;
    private Long lessonVersionId;
    private String resourcesUrl;
    private String resourcesType; // video, audio, document, image, link, other
    private Integer sortOrder;
    private String status; // active, inactive
    private Integer isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
