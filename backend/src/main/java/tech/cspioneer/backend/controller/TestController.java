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
    public ResponseEntity<ApiResponse<String>> getMyUuid(@AuthenticationPrincipal UserDetails userDetails) {
        // 如果token无效或未提供，JwtAuthenticationFilter会拦截请求，
        // 根本不会执行到这里的代码。
        // 如果执行到这里，说明认证成功。
        // userDetails.getUsername() 在我们的实现中返回的是用户的UUID。
        String userUuid = userDetails.getUsername();
        return ResponseEntity.ok(ApiResponse.success(200, "Successfully authenticated.", userUuid));
    }
}
