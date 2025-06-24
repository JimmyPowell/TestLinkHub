package tech.cspioneer.backend.entity.dto.request;

import lombok.Data;

@Data
public class AnnouncementRequest {
    private String title;
    private String content;
    // In the future, this could be an enum: ALL, COMPANY, etc.
    // For now, we can keep it simple.
    private String targetAudience = "ALL"; 
    private Long companyId; // Optional, for targeting a specific company
}
