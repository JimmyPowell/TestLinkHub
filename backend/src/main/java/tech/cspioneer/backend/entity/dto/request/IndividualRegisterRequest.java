package tech.cspioneer.backend.entity.dto.request;

import lombok.Data;
import tech.cspioneer.backend.entity.enums.Gender;
import tech.cspioneer.backend.entity.enums.UserRole;
import tech.cspioneer.backend.entity.enums.UserStatus;

import java.time.LocalDateTime;

@Data
public class IndividualRegisterRequest {
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private String address;
    private String avatarUrl;
    private Gender gender;
    private Long companyId;
    private String description;

}
