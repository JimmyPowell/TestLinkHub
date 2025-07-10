package tech.cspioneer.backend.entity.dto.request;

import lombok.Data;

@Data
public class RootUserSearchRequest {
    private String uuid;
    private String username;
    private String status;
    private String phoneNumber;
    private String companyName;
    private String companyUuid;
}
