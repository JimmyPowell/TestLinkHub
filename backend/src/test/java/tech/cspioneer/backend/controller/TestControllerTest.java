package tech.cspioneer.backend.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import tech.cspioneer.backend.utils.JwtUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;

@WebMvcTest(TestController.class)
public class TestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtils jwtUtils;

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable());
            return http.build();
        }
    }

    @BeforeEach
    void setupSecurityContext() {
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken("mock-uuid", null, Collections.singleton(new SimpleGrantedAuthority("USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("获取当前用户UUID-需要认证")
    void testGetMyUuid() throws Exception {
        mockMvc.perform(get("/api/test/me"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("只有COMPANY身份能访问")
    void testGetCompanyOnly() throws Exception {
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken("mock-company-uuid", null, Collections.singleton(new SimpleGrantedAuthority("COMPANY")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        mockMvc.perform(get("/api/test/company-only"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("获取用户详情-需要认证")
    void testGetUserDetails() throws Exception {
        mockMvc.perform(get("/api/test/user-detail"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("获取用户信息-需要认证")
    void testGetUserInfo() throws Exception {
        mockMvc.perform(get("/api/test/user-info"))
                .andExpect(status().isOk());
    }
} 