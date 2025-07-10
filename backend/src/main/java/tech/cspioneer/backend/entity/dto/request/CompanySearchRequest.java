package tech.cspioneer.backend.entity.dto.request;

import lombok.Data;

@Data
public class CompanySearchRequest {
    private String uuid;
    private String name;
    private String status;
    private String companyCode;
}
