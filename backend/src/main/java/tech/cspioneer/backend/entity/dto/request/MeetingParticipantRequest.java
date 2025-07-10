package tech.cspioneer.backend.entity.dto.request;


import lombok.Data;

@Data
public class MeetingParticipantRequest {
    private String meetingUuid;
    private String joinReason;


}
