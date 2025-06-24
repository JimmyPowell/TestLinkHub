package tech.cspioneer.backend.entity.dto.request;

import lombok.Data;

@Data
public class MeetingCreateRequest {
    private String name;
    private String description;
    private String startTime;
    private String endTime;
    private String imageUrl;
    private Integer visiable;
}
