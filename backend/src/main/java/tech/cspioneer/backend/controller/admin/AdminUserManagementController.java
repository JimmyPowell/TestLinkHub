package tech.cspioneer.backend.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.entity.dto.request.UserCreateRequest;
import tech.cspioneer.backend.entity.dto.request.UserUpdateRequest;
import tech.cspioneer.backend.entity.dto.response.UserResponse;
import tech.cspioneer.backend.exception.ResourceNotFoundException;
import tech.cspioneer.backend.exception.UserManagementException;
import tech.cspioneer.backend.model.response.ApiResponse;
import tech.cspioneer.backend.model.response.PagedResponse;
import tech.cspioneer.backend.service.AdminUserManagementService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserManagementController {

    private final AdminUserManagementService adminUserManagementService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('COMPANY', 'ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                                                                 @RequestParam(defaultValue = "10") int size) {
        try {
            PagedResponse<UserResponse> users = adminUserManagementService.getAllUsers(page, size);
            return ResponseEntity.ok(ApiResponse.success(200, "Users retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, e.getMessage()));
        }
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasAnyAuthority('COMPANY', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByUuid(@PathVariable String uuid) {
        try {
            UserResponse user = adminUserManagementService.getUserByUuid(uuid);
            return ResponseEntity.ok(ApiResponse.success(200, "User retrieved successfully", user));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, "An unexpected error occurred."));
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('COMPANY', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody UserCreateRequest userCreateRequest) {
        try {
            UserResponse createdUser = adminUserManagementService.createUser(userCreateRequest);
            return ResponseEntity.ok(ApiResponse.success(201, "User created successfully", createdUser));
        } catch (UserManagementException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, "An unexpected error occurred during user creation."));
        }
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasAnyAuthority('COMPANY', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable String uuid, @RequestBody UserUpdateRequest userUpdateRequest) {
        try {
            UserResponse updatedUser = adminUserManagementService.updateUser(uuid, userUpdateRequest);
            return ResponseEntity.ok(ApiResponse.success(200, "User updated successfully", updatedUser));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, e.getMessage()));
        } catch (UserManagementException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, "An unexpected error occurred during user update."));
        }
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasAnyAuthority('COMPANY', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String uuid) {
        try {
            adminUserManagementService.deleteUser(uuid);
            return ResponseEntity.ok(ApiResponse.success(204, "User deleted successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, "An unexpected error occurred during user deletion."));
        }
    }
}
