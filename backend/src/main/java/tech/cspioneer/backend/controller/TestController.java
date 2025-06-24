package tech.cspioneer.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.cspioneer.backend.model.response.ApiResponse;

@RestController
@RequestMapping("/api/test")
public class TestController {

    /**
     * 一个受保护的端点，用于测试身份认证。
     * 访问此端点需要有效的JWT。
     * @param userDetails 由JwtAuthenticationFilter注入的当前用户信息
     * @return 包含用户UUID的响应
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<String>> getMyUuid(@AuthenticationPrincipal String userUuid) {
        // 在无状态JWT实现中，Principal直接就是用户的UUID字符串
        return ResponseEntity.ok(ApiResponse.success(200, "Successfully authenticated.", userUuid));
    }

    /**
     * 一个只有COMPANY身份才能访问的端点。
     * @param companyUuid 由JwtAuthenticationFilter注入的当前公司UUID
     * @return 包含公司UUID的响应
     */
    @GetMapping("/company-only")
    public ResponseEntity<ApiResponse<String>> getCompanyOnly(@AuthenticationPrincipal String companyUuid) {
        // SecurityConfig中配置了此端点需要 "COMPANY" 权限
        // 如果能执行到这里，说明权限验证通过
        return ResponseEntity.ok(ApiResponse.success(200, "Access granted for company.", companyUuid));
    }
}
