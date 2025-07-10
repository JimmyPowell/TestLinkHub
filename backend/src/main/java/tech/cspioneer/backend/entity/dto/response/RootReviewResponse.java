package tech.cspioneer.backend.entity.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RootReviewResponse {
    private String uuid;  //meeting_version_uuid
    private String meetingUuid;     //meeting_uuid，需要根据meeting_id查询
    private Integer version;
    private String name;
    private String description;
    private String coverImageUrl;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String initiatorName;   // 发起者名称
    private LocalDateTime createdAt;

}
