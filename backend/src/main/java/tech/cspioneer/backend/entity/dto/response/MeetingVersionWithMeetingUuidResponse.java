package tech.cspioneer.backend.entity.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MeetingVersionWithMeetingUuidResponse {
    // Fields from MeetingVersion
    private Long id;
    private String uuid;
    private Long meetingId;
    private Integer version;
    private String name;
    private String description;
    private String coverImageUrl;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private Long editorId;
    private LocalDateTime createdAt;
    private Integer isDeleted;

    // Additional field for the parent meeting's UUID
    private String meetingUuid;
}
