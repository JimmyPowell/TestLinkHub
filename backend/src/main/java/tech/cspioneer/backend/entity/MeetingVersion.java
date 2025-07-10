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
public class MeetingVersion {
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

}
