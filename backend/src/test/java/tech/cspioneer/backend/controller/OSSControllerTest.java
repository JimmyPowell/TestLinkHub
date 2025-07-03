package tech.cspioneer.backend.controller;

import com.aliyuncs.exceptions.ClientException;
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
import tech.cspioneer.backend.entity.STSTemporaryCredentials;
import tech.cspioneer.backend.service.OSSService;
import tech.cspioneer.backend.utils.JwtUtils;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OSSController.class)
public class OSSControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private OSSService ossService;
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

    @Test
    @DisplayName("STS凭证获取-正常")
    void testGetSTSCredentials_Success() throws Exception {
        when(ossService.getSTSCredentials()).thenReturn(new STSTemporaryCredentials());
        mockMvc.perform(get("/api/oss/sts"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("STS凭证获取-异常")
    void testGetSTSCredentials_Exception() throws Exception {
        when(ossService.getSTSCredentials()).thenThrow(new ClientException("error"));
        mockMvc.perform(get("/api/oss/sts"))
                .andExpect(status().isInternalServerError());
    }
} 