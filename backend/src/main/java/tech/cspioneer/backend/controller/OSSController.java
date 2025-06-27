package tech.cspioneer.backend.controller;

import com.aliyuncs.exceptions.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.cspioneer.backend.entity.STSTemporaryCredentials;
import tech.cspioneer.backend.model.response.ApiResponse;
import tech.cspioneer.backend.service.OSSService;

@RestController
@RequestMapping("/api/oss")
@RequiredArgsConstructor
public class OSSController {

    private final OSSService ossService;

    @GetMapping("/sts")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'COMPANY')")
    public ResponseEntity<ApiResponse<STSTemporaryCredentials>> getSTSCredentials() {
        try {
            STSTemporaryCredentials credentials = ossService.getSTSCredentials();
            return ResponseEntity.ok(ApiResponse.success(200, "STS credentials generated successfully.", credentials));
        } catch (ClientException e) {
            // Log the exception for debugging purposes
            // log.error("Error generating STS credentials", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error(500, "Failed to generate STS credentials: " + e.getMessage()));
        }
    }
}
