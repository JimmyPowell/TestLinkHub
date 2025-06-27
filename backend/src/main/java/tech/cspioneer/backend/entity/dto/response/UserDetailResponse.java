package tech.cspioneer.backend.entity.dto.response;

import lombok.Data;
import tech.cspioneer.backend.entity.enums.Gender;
import tech.cspioneer.backend.entity.enums.UserRole;
import tech.cspioneer.backend.entity.enums.UserStatus;

import java.time.LocalDateTime;

@Data
public class UserDetailResponse {
    private String uuid;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private String avatarUrl;
    private Gender gender;
    private String companyName; // Changed from companyId
    private UserRole role;
    private UserStatus status;
    private String description;
    private Integer postCount;
    private Integer lessonCount;
    private Integer meetingCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
