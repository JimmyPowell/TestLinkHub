package tech.cspioneer.backend.entity.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MeetingPartResponse {
    private String uuid;     //meeting_participant_uuid
    private String meetingUuid;
    private String userUuid;
    private String joinReason;
    private String status;
    private String reviewComment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
