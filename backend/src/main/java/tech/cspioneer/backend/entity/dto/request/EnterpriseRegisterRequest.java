package tech.cspioneer.backend.entity.dto.request;


import lombok.Data;
import tech.cspioneer.backend.entity.enums.CompanyStatus;

import java.time.LocalDateTime;

@Data
public class EnterpriseRegisterRequest {
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private String address;
    private String avatarUrl;
    private String companyCode;
    private String description;

}
