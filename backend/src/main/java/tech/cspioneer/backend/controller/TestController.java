package tech.cspioneer.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.cspioneer.backend.model.response.ApiResponse;

import java.util.HashMap;
import java.util.Map;

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
    
    /**
     * 获取当前用户的UUID和身份信息
     * @return 包含用户UUID和身份的响应
     */
    @GetMapping("/user-detail")
    public ResponseEntity<ApiResponse<Map<String, String>>> getUserDetails() {
        // 从SecurityContextHolder获取认证信息
        System.out.println("aaa");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String uuid = (String) authentication.getPrincipal();
        System.out.println(uuid);



            
            // 获取用户身份/角色
        String identity = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(authority -> authority.getAuthority())
                    .orElse("UNKNOWN");

        System.out.println(identity);


        return ResponseEntity.ok(ApiResponse.success(200, "User details retrieved successfully", null));
    }

    /**
     * 使用@AuthenticationPrincipal直接注入参数的方式获取用户信息
     * @param userUuid 注入的用户UUID
     * @param authentication 完整的认证对象
     * @return 用户信息
     */



    @GetMapping("/user-info")
    public ResponseEntity<ApiResponse<Map<String, String>>> getUserInfo(
            @AuthenticationPrincipal String userUuid,
            Authentication authentication) {
        
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("uuid", userUuid);
        
        // 从authentication对象获取身份信息
        if (authentication != null && authentication.isAuthenticated()) {
            String identity = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(authority -> authority.getAuthority())
                    .orElse("UNKNOWN");
            userInfo.put("identity", identity);
        }
        
        return ResponseEntity.ok(ApiResponse.success(200, "User information retrieved", userInfo));
    }
}
