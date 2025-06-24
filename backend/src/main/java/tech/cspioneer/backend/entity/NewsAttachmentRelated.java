package tech.cspioneer.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsAttachmentRelated {
    private Long id;
    private Long newsContentId;
    private Long attachmentId;
    private int isDeleted;
    private LocalDateTime createdAt;
}