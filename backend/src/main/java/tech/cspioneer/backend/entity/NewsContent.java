package tech.cspioneer.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.cspioneer.backend.entity.enums.NewsContentStatus;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsContent {
    private Long id;
    private String uuid;
    private Long newsId;
    private String title;
    private String summary;
    private String coverImageUrl;
    private String resourceUrl;
    private int version;
    private NewsContentStatus status;
    private Long publisherId;
    private int isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
