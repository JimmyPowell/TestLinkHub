package tech.cspioneer.backend.controller.root;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.entity.dto.request.CompanyCreateRequest;
import tech.cspioneer.backend.entity.dto.request.CompanySearchRequest;
import tech.cspioneer.backend.entity.dto.request.CompanyUpdateRequest;
import tech.cspioneer.backend.entity.dto.response.CompanyResponse;
import tech.cspioneer.backend.exception.ResourceNotFoundException;
import tech.cspioneer.backend.exception.UserManagementException;
import tech.cspioneer.backend.model.response.ApiResponse;
import tech.cspioneer.backend.model.response.PagedResponse;
import tech.cspioneer.backend.service.RootCompanyManagementService;

import java.util.Map;

@RestController
@RequestMapping("/api/root/companies")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class RootCompanyManagementController {

    private final RootCompanyManagementService companyService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<CompanyResponse>>> getAllCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            CompanySearchRequest request) {
        PagedResponse<CompanyResponse> companies = companyService.getAllCompanies(page, size, request);
        return ResponseEntity.ok(ApiResponse.success(200, "Companies retrieved successfully", companies));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<CompanyResponse>> getCompanyByUuid(@PathVariable String uuid) {
        try {
            CompanyResponse company = companyService.getCompanyByUuid(uuid);
            return ResponseEntity.ok(ApiResponse.success(200, "Company retrieved successfully", company));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(404, e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CompanyResponse>> createCompany(@Valid @RequestBody CompanyCreateRequest createRequest) {
        try {
            CompanyResponse createdCompany = companyService.createCompany(createRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(201, "Company created successfully", createdCompany));
        } catch (UserManagementException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ApiResponse<CompanyResponse>> updateCompany(@PathVariable String uuid, @Valid @RequestBody CompanyUpdateRequest updateRequest) {
        try {
            CompanyResponse updatedCompany = companyService.updateCompany(uuid, updateRequest);
            return ResponseEntity.ok(ApiResponse.success(200, "Company updated successfully", updatedCompany));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(404, e.getMessage()));
        } catch (UserManagementException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deleteCompany(@PathVariable String uuid) {
        try {
            companyService.deleteCompany(uuid);
            return ResponseEntity.ok(ApiResponse.success(204, "Company deleted successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(404, e.getMessage()));
        }
    }

    @PutMapping("/{uuid}/status")
    public ResponseEntity<ApiResponse<CompanyResponse>> updateCompanyStatus(@PathVariable String uuid, @RequestBody Map<String, String> statusMap) {
        try {
            String status = statusMap.get("status");
            if (status == null || status.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "Status cannot be empty."));
            }
            CompanyResponse updatedCompany = companyService.updateCompanyStatus(uuid, status);
            return ResponseEntity.ok(ApiResponse.success(200, "Company status updated successfully", updatedCompany));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(404, e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Invalid status value."));
        }
    }
}
