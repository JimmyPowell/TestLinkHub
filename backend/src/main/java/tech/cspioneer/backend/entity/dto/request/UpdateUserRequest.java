package tech.cspioneer.backend.entity.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String name;
    private String phoneNumber;
    private String address;
    private String avatarUrl;
    private String gender;
    private String description;
}
