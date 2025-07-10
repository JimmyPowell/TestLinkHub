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
public class LessonVersion {
    private Long id;
    private String uuid;
    private Long lessonId;
    private Integer version;
    private String name;
    private String description;
    private String imageUrl;
    private String authorName;
    private Integer sortOrder;
    private String status; // draft, pending_review, active, rejected, archived
    private Long creatorId;
    private Integer isDeleted;
    private LocalDateTime createdAt;
}
