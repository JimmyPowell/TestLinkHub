package tech.cspioneer.backend.controller.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import tech.cspioneer.backend.entity.dto.response.NewsDetailResponse;
import tech.cspioneer.backend.entity.dto.response.NewsListResponse;
import tech.cspioneer.backend.service.NewsService;
import java.util.Collections;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientNewsController.class)
public class ClientNewsControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private NewsService newsService;
    @MockBean
    private tech.cspioneer.backend.utils.JwtUtils jwtUtils;
    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable());
            return http.build();
        }
    }

    private void setAuth(String role) {
        Authentication auth = new UsernamePasswordAuthenticationToken("mock-uuid", null, Collections.singleton(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("getNewsList-正常")
    void testGetNewsList_Success() throws Exception {
        setAuth("USER");
        when(newsService.getNewsList(any())).thenReturn(List.of(new NewsListResponse()));
        mockMvc.perform(get("/api/user/news/newsList"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("getNewsDetail-正常")
    void testGetNewsDetail_Success() throws Exception {
        setAuth("COMPANY");
        when(newsService.getNewsDetail(anyString(), anyString(), anyString())).thenReturn(new NewsDetailResponse());
        mockMvc.perform(get("/api/user/news/getNews/uuid-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
} 