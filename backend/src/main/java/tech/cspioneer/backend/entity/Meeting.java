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
public class Meeting {
    private Long id;
    private String uuid;
    private Long creatorId;
    private String status;
    private Long currentVersionId;
    private Long pendingVersionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer isDeleted;

}
