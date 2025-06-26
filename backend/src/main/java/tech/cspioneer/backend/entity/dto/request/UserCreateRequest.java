package tech.cspioneer.backend.entity.dto.request;

import lombok.Data;
import tech.cspioneer.backend.entity.enums.Gender;
import tech.cspioneer.backend.entity.enums.UserRole;
import tech.cspioneer.backend.entity.enums.UserStatus;

@Data
public class UserCreateRequest {
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private String address;
    private String avatarUrl;
    private Gender gender;
    private Long companyId;
    private UserRole role;
    private UserStatus status;
    private String description;
}
