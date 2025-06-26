package tech.cspioneer.backend.entity.dto.request;

import lombok.Data;
import tech.cspioneer.backend.entity.enums.Gender;
import tech.cspioneer.backend.entity.enums.UserRole;
import tech.cspioneer.backend.entity.enums.UserStatus;

@Data
public class UserUpdateRequest {
    private String name;
    private String phoneNumber;
    private String address;
    private String avatarUrl;
    private Gender gender;
    private Long companyId;
    private UserRole role;
    private UserStatus status;
    private String description;
}
