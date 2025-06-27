package tech.cspioneer.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class STSTemporaryCredentials {
    private String accessKeyId;
    private String accessKeySecret;
    private String securityToken;
    private String expiration;
    private String region;
    private String bucketName;
}
