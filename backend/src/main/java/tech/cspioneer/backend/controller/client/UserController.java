package tech.cspioneer.backend.controller.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.cspioneer.backend.entity.dto.response.CurrentUserResponse;
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CurrentUserResponse>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "User not authenticated."));
        }

        String uuid = authentication.getName(); // The 'name' is the UUID we set in UserDetails
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("User has no authorities"))
                .getAuthority();

        if (uuid == null || role == null) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "Invalid token payload."));
        }

        try {
            CurrentUserResponse userInfo = userService.getCurrentUserInfo(uuid, role);
            return ResponseEntity.ok(ApiResponse.success(200, "User info retrieved successfully", userInfo));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, "An unexpected error occurred."));
        }
    }
}
