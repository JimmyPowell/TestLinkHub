package tech.cspioneer.backend.entity.dto.request;

import lombok.Data;

@Data
public class MeetingUpdateRequest {
    private String name;
    private String description;
    private String startTime;
    private String endTime;

    private String meetingUuid;
    private Long editorId;


    private String imageUrl;
    private Integer visiable;
}
