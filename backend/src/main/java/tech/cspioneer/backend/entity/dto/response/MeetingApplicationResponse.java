package tech.cspioneer.backend.entity.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MeetingApplicationResponse {
    // Fields from MeetingParticipant
    private String uuid;
    private String joinReason;
    private String status;
    private LocalDateTime createdAt;

    // Additional fields
    private String meetingName;
    private String meetingUuid;
    private String applicantName;
    private String applicantUuid;
    private String applicantEmail;
}
