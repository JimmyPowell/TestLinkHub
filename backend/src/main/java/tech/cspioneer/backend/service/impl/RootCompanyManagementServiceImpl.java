package tech.cspioneer.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import tech.cspioneer.backend.entity.Company;
import tech.cspioneer.backend.entity.dto.request.CompanyCreateRequest;
import tech.cspioneer.backend.entity.dto.request.CompanySearchRequest;
import tech.cspioneer.backend.entity.dto.request.CompanyUpdateRequest;
import tech.cspioneer.backend.entity.dto.response.CompanyResponse;
import tech.cspioneer.backend.exception.ResourceNotFoundException;
import tech.cspioneer.backend.exception.UserManagementException;
import tech.cspioneer.backend.mapper.RootCompanyManagementMapper;
import tech.cspioneer.backend.model.response.PagedResponse;
import tech.cspioneer.backend.service.RootCompanyManagementService;
import tech.cspioneer.backend.utils.UuidUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RootCompanyManagementServiceImpl implements RootCompanyManagementService {

    private final RootCompanyManagementMapper companyMapper;

    @Override
    public PagedResponse<CompanyResponse> getAllCompanies(int page, int size, CompanySearchRequest request) {
        int offset = page * size;
        List<CompanyResponse> companies = companyMapper.findWithFilters(request, size, offset);
        long totalElements = companyMapper.countWithFilters(request);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean isLast = page >= totalPages - 1;

        return new PagedResponse<>(companies, page, size, totalElements, totalPages, isLast);
    }

    @Override
    public CompanyResponse getCompanyByUuid(String uuid) {
        Company company = companyMapper.findByUuid(uuid);
        if (company == null) {
            throw new ResourceNotFoundException("Company not found with UUID: " + uuid);
        }
        CompanyResponse companyResponse = new CompanyResponse();
        BeanUtils.copyProperties(company, companyResponse);
        return companyResponse;
    }

    @Override
    public CompanyResponse createCompany(CompanyCreateRequest createRequest) {
        if (companyMapper.findByCompanyCode(createRequest.getCompanyCode()) != null) {
            throw new UserManagementException("Company code already exists: " + createRequest.getCompanyCode());
        }

        Company company = new Company();
        BeanUtils.copyProperties(createRequest, company);
        company.setUuid(UuidUtils.randomUuidWithoutDash());

        companyMapper.insert(company);

        CompanyResponse response = new CompanyResponse();
        BeanUtils.copyProperties(company, response);
        return response;
    }

    @Override
    public CompanyResponse updateCompany(String uuid, CompanyUpdateRequest updateRequest) {
        CompanyResponse existingCompanyCheck = getCompanyByUuid(uuid); // This now correctly returns a DTO after checking existence
        Company existingCompany = companyMapper.findByUuid(uuid); // Get the entity for business logic

        // Check if company code is being changed to one that already exists
        if (updateRequest.getCompanyCode() != null && !updateRequest.getCompanyCode().equals(existingCompany.getCompanyCode())) {
            throw new ResourceNotFoundException("Company not found with UUID: " + uuid);
        }

        // Check if company code is being changed to one that already exists
        if (updateRequest.getCompanyCode() != null && !updateRequest.getCompanyCode().equals(existingCompany.getCompanyCode())) {
            if (companyMapper.findByCompanyCode(updateRequest.getCompanyCode()) != null) {
                throw new UserManagementException("Company code already exists: " + updateRequest.getCompanyCode());
            }
        }

        Company companyToUpdate = new Company();
        BeanUtils.copyProperties(updateRequest, companyToUpdate);
        companyToUpdate.setUuid(uuid); // Ensure UUID is set for the update
        
        companyMapper.update(companyToUpdate);

        return getCompanyByUuid(uuid);
    }

    @Override
    public void deleteCompany(String uuid) {
        Company existingCompany = companyMapper.findByUuid(uuid);
        if (existingCompany == null) {
            throw new ResourceNotFoundException("Company not found with UUID: " + uuid);
        }
        companyMapper.softDelete(uuid);
    }

    @Override
    public CompanyResponse updateCompanyStatus(String uuid, String status) {
        Company existingCompany = companyMapper.findByUuid(uuid);
        if (existingCompany == null) {
            throw new ResourceNotFoundException("Company not found with UUID: " + uuid);
        }
        companyMapper.updateStatus(uuid, status);
        return getCompanyByUuid(uuid);
    }
}
