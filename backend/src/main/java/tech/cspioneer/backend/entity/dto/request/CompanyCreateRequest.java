package tech.cspioneer.backend.entity.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import tech.cspioneer.backend.entity.enums.CompanyStatus;

@Data
public class CompanyCreateRequest {

    @NotEmpty(message = "Company name cannot be empty.")
    private String name;

    @Email(message = "Invalid email format.")
    private String email;

    private String phoneNumber;

    private String address;

    private String avatarUrl;

    @NotEmpty(message = "Company code cannot be empty.")
    private String companyCode;

    private CompanyStatus status = CompanyStatus.PENDING;

    private String description;
}
