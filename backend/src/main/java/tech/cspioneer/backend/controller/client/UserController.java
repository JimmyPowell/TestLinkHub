package tech.cspioneer.backend.controller.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.cspioneer.backend.entity.dto.request.UpdateUserRequest;
import tech.cspioneer.backend.entity.dto.response.CurrentUserResponse;
import tech.cspioneer.backend.entity.dto.response.UserInfoResponse;
import tech.cspioneer.backend.exception.ResourceNotFoundException;
import tech.cspioneer.backend.model.response.ApiResponse;
import tech.cspioneer.backend.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api/client/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("hasAnyAuthority('USER', 'COMPANY_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String uuid = authentication.getName();

        try {
            UserInfoResponse userInfo = userService.getUserInfo(uuid);
            return ResponseEntity.ok(ApiResponse.success(200, "User info retrieved successfully", userInfo));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(500, "An unexpected error occurred."));
        }
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyAuthority('USER', 'COMPANY_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<UserInfoResponse>> updateCurrentUser(@RequestBody UpdateUserRequest updateUserRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String uuid = authentication.getName();

        try {
            UserInfoResponse updatedUserInfo = userService.updateUserInfo(uuid, updateUserRequest);
            return ResponseEntity.ok(ApiResponse.success(200, "User info updated successfully", updatedUserInfo));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(500, "An unexpected error occurred."));
        }
    }
}
