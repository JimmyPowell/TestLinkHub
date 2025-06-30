package tech.cspioneer.backend.entity.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;
import tech.cspioneer.backend.entity.enums.CompanyStatus;

@Data
public class CompanyUpdateRequest {

    private String name;

    @Email(message = "Invalid email format.")
    private String email;

    private String phoneNumber;

    private String address;

    private String avatarUrl;

    private String companyCode;

    private CompanyStatus status;

    private String description;
}
