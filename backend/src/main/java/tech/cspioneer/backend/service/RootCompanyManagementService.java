package tech.cspioneer.backend.service;

import tech.cspioneer.backend.entity.dto.request.CompanyCreateRequest;
import tech.cspioneer.backend.entity.dto.request.CompanySearchRequest;
import tech.cspioneer.backend.entity.dto.request.CompanyUpdateRequest;
import tech.cspioneer.backend.entity.dto.response.CompanyResponse;
import tech.cspioneer.backend.model.response.PagedResponse;

public interface RootCompanyManagementService {

    PagedResponse<CompanyResponse> getAllCompanies(int page, int size, CompanySearchRequest request);

    CompanyResponse getCompanyByUuid(String uuid);

    CompanyResponse createCompany(CompanyCreateRequest createRequest);

    CompanyResponse updateCompany(String uuid, CompanyUpdateRequest updateRequest);

    void deleteCompany(String uuid);

    CompanyResponse updateCompanyStatus(String uuid, String status);
}
