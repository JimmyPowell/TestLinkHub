package tech.cspioneer.backend.controller.root;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.entity.dto.request.RootUserSearchRequest;
import tech.cspioneer.backend.entity.dto.request.UserCreateRequest;
import tech.cspioneer.backend.entity.dto.request.UserUpdateRequest;
import tech.cspioneer.backend.entity.dto.response.UserResponse;
import tech.cspioneer.backend.exception.ResourceNotFoundException;
import tech.cspioneer.backend.exception.UserManagementException;
import tech.cspioneer.backend.model.response.ApiResponse;
import tech.cspioneer.backend.model.response.PagedResponse;
import tech.cspioneer.backend.service.RootUserManagementService;

@RestController
@RequestMapping("/api/root/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class RootUserManagementController {

    private final RootUserManagementService rootUserManagementService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            RootUserSearchRequest request) {
        try {
            PagedResponse<UserResponse> users = rootUserManagementService.getAllUsers(page, size, request);
            return ResponseEntity.ok(ApiResponse.success(200, "Users retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, e.getMessage()));
        }
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByUuid(@PathVariable String uuid) {
        try {
            UserResponse user = rootUserManagementService.getUserByUuid(uuid);
            return ResponseEntity.ok(ApiResponse.success(200, "User retrieved successfully", user));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, "An unexpected error occurred."));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody UserCreateRequest userCreateRequest) {
        try {
            UserResponse createdUser = rootUserManagementService.createUser(userCreateRequest);
            return ResponseEntity.ok(ApiResponse.success(201, "User created successfully", createdUser));
        } catch (UserManagementException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, "An unexpected error occurred during user creation."));
        }
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable String uuid, @RequestBody UserUpdateRequest userUpdateRequest) {
        try {
            UserResponse updatedUser = rootUserManagementService.updateUser(uuid, userUpdateRequest);
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
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String uuid) {
        try {
            rootUserManagementService.deleteUser(uuid);
            return ResponseEntity.ok(ApiResponse.success(204, "User deleted successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, "An unexpected error occurred during user deletion."));
        }
    }

    @GetMapping("/company/{companyUuid}")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getUsersByCompanyUuid(
            @PathVariable String companyUuid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            RootUserSearchRequest request = new RootUserSearchRequest();
            request.setCompanyUuid(companyUuid);
            PagedResponse<UserResponse> users = rootUserManagementService.getAllUsers(page, size, request);
            return ResponseEntity.ok(ApiResponse.success(200, "Users retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, e.getMessage()));
        }
    }
}
