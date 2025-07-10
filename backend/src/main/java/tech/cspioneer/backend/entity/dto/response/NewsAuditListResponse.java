package tech.cspioneer.backend.entity.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class NewsAuditListResponse {
    private String uuid;
    private String newsContentUuid;
    private Long companyId;
    private String companyName; // 新增
    private Long publisherId;
    private String publisherName; // 新增
    private String title;
    private String summary;
    private String coverImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime contentCreatedAt;
    private int version;
    private String status;
}
