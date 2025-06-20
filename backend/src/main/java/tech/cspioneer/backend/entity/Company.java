package tech.cspioneer.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.cspioneer.backend.entity.enums.CompanyStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    private Long id;
    private String uuid;
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private String address;
    private String avatarUrl;
    private String companyCode;
    private CompanyStatus status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
