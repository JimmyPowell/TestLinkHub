package tech.cspioneer.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    /**
     * 密码编码器
     * @return BCryptPasswordEncoder 实例
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * 配置 HTTP 安全规则
     * @param http HttpSecurity 配置对象
     * @return SecurityFilterChain 实例
     * @throws Exception 如果配置出错
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173")); // Allow frontend origin
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 启用CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // 禁用 CSRF 保护，因为我们使用的是无状态 API
            .csrf(AbstractHttpConfigurer::disable)
            // 配置会话管理，使用无状态会话
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // 配置授权规则
            .authorizeHttpRequests(authorize -> authorize
                // 允许所有人访问 Auth 控制器的所有端点
                .requestMatchers("/api/auth/**").permitAll()
                // 具体放开 Auth 控制器中的端点
                .requestMatchers("/api/auth/generate-verify-code").permitAll()
                .requestMatchers("/api/auth/verify-code").permitAll()
                    .requestMatchers("/api/auth/register/individual").permitAll()
                .requestMatchers("/api/auth/register/individual").permitAll()
                .requestMatchers("/api/auth/register/enterprise").permitAll() // 预留企业注册端点
                .requestMatchers("/api/auth/login/user").permitAll()
                .requestMatchers("/api/auth/login/company").permitAll()
                .requestMatchers("/api/auth/refresh").permitAll()
                .requestMatchers("/api/auth/logout").permitAll()
                .requestMatchers("/api/auth/forgot-password").permitAll() // 预留忘记密码端点
                    .requestMatchers("/api/user/lesson/**").permitAll()
                // 允许所有人访问 Swagger UI 和 API 文档
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // 允许所有人访问静态资源
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()



                    // 配置所有/api/admin/路径下的端点权限
                .requestMatchers("/api/admin/**").hasAnyAuthority("ADMIN", "COMPANY")
                // 配置测试端点权限
                .requestMatchers("/api/test/company-only").hasAuthority("COMPANY")
                //配置课程权限
                    .requestMatchers("/api/admin/lesson/**")
                    .access((auth, context) -> {
                        var authorities = auth.get().getAuthorities();
                        boolean ok = authorities.stream().anyMatch(a ->
                                "ADMIN".equals(a.getAuthority()) || "COMPANY".equals(a.getAuthority())
                        );
                        return new org.springframework.security.authorization.AuthorizationDecision(ok);
                    })

                    .requestMatchers("/api/root/lesson/**").hasAuthority("ADMIN")


                // 其他所有请求需要认证
                .anyRequest().authenticated()
            )
            // 禁用 HTTP Basic 认证
            .httpBasic(AbstractHttpConfigurer::disable)
            // 禁用表单登录
            .formLogin(AbstractHttpConfigurer::disable)
            // 添加JWT过滤器
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            // 配置异常处理
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint())
            );
            
        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        };
    }
}
