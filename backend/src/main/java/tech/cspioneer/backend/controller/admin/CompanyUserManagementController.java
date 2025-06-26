package tech.cspioneer.backend.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.entity.dto.request.UserCreateRequest;
import tech.cspioneer.backend.entity.dto.request.UserUpdateRequest;
import tech.cspioneer.backend.entity.dto.response.UserResponse;
import tech.cspioneer.backend.exception.ResourceNotFoundException;
import tech.cspioneer.backend.exception.UserManagementException;
import tech.cspioneer.backend.model.response.ApiResponse;
import tech.cspioneer.backend.model.response.PagedResponse;
import tech.cspioneer.backend.service.CompanyUserManagementService;

@RestController
@RequestMapping("/api/admin/company/users")
@RequiredArgsConstructor
public class CompanyUserManagementController {

    private final CompanyUserManagementService companyUserManagementService;

    @GetMapping
    @PreAuthorize("hasAuthority('COMPANY')")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getCompanyUsers(@RequestParam(defaultValue = "0") int page,
                                                                                     @RequestParam(defaultValue = "10") int size,
                                                                                     @AuthenticationPrincipal String currentUserUuid) {
        try {
            PagedResponse<UserResponse> users = companyUserManagementService.getCompanyUsers(page, size, currentUserUuid);
            return ResponseEntity.ok(ApiResponse.success(200, "Company users retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('COMPANY')")
    public ResponseEntity<ApiResponse<UserResponse>> createCompanyUser(@RequestBody UserCreateRequest userCreateRequest,
                                                                       @AuthenticationPrincipal String currentUserUuid) {
        try {
            UserResponse createdUser = companyUserManagementService.createCompanyUser(userCreateRequest, currentUserUuid);
            return ResponseEntity.ok(ApiResponse.success(201, "User created successfully for the company", createdUser));
        } catch (UserManagementException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, "An unexpected error occurred during user creation."));
        }
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasAuthority('COMPANY')")
    public ResponseEntity<ApiResponse<UserResponse>> updateCompanyUser(@PathVariable String uuid,
                                                                       @RequestBody UserUpdateRequest userUpdateRequest,
                                                                       @AuthenticationPrincipal String currentUserUuid) {
        try {
            UserResponse updatedUser = companyUserManagementService.updateCompanyUser(uuid, userUpdateRequest, currentUserUuid);
            return ResponseEntity.ok(ApiResponse.success(200, "Company user updated successfully", updatedUser));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, e.getMessage()));
        } catch (UserManagementException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, "An unexpected error occurred during user update."));
        }
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasAuthority('COMPANY')")
    public ResponseEntity<ApiResponse<Void>> deleteCompanyUser(@PathVariable String uuid,
                                                               @AuthenticationPrincipal String currentUserUuid) {
        try {
            companyUserManagementService.deleteCompanyUser(uuid, currentUserUuid);
            return ResponseEntity.ok(ApiResponse.success(204, "Company user deleted successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, "An unexpected error occurred during user deletion."));
        }
    }

    @PostMapping("/{uuid}/remove")
    @PreAuthorize("hasAuthority('COMPANY')")
    public ResponseEntity<ApiResponse<Void>> removeUserFromCompany(@PathVariable String uuid,
                                                                   @AuthenticationPrincipal String currentUserUuid) {
        try {
            companyUserManagementService.removeUserFromCompany(uuid, currentUserUuid);
            return ResponseEntity.ok(ApiResponse.success(200, "User successfully removed from the company", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, "An unexpected error occurred while removing user from company."));
        }
    }
}
