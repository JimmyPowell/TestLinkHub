package tech.cspioneer.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
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
                .requestMatchers("/api/auth/login").permitAll() // 预留登录端点
                .requestMatchers("/api/auth/forgot-password").permitAll() // 预留忘记密码端点
                // 允许所有人访问 Swagger UI 和 API 文档
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // 允许所有人访问静态资源
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                // 其他所有请求需要认证
                .anyRequest().authenticated()
            )
            // 禁用 HTTP Basic 认证
            .httpBasic(AbstractHttpConfigurer::disable)
            // 禁用表单登录
            .formLogin(AbstractHttpConfigurer::disable);
            
        return http.build();
    }
}
