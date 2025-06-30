package tech.cspioneer.backend.entity.dto.response;

import lombok.Data;
import tech.cspioneer.backend.entity.enums.CompanyStatus;

import java.time.LocalDateTime;

@Data
public class CompanyResponse {
    private Long id;
    private String uuid;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private String avatarUrl;
    private String companyCode;
    private CompanyStatus status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
