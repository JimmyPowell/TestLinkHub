package tech.cspioneer.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.cspioneer.backend.entity.enums.CompanyStatus;
import tech.cspioneer.backend.entity.enums.NewsStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class News {
    private Long id;
    private String uuid;
    private Long companyId;
    private int visible;
    private NewsStatus status;
    private Long currentContendId;
    private Long pendingContendId;
    private int isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
