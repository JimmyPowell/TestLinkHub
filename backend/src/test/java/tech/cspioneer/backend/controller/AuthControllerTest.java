package tech.cspioneer.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.security.authentication.BadCredentialsException;
import tech.cspioneer.backend.entity.dto.request.*;
import tech.cspioneer.backend.entity.dto.response.LoginResponse;
import tech.cspioneer.backend.exception.VerificationCodeException;
import tech.cspioneer.backend.model.response.ApiResponse;
import tech.cspioneer.backend.service.AuthService;
import tech.cspioneer.backend.utils.JwtUtils;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private JwtUtils jwtUtils;
    @MockBean
    private AuthService authService;
    @Autowired
    private ObjectMapper objectMapper;

    @org.springframework.boot.test.context.TestConfiguration
    static class TestSecurityConfig {
        @org.springframework.context.annotation.Bean
        public org.springframework.security.web.SecurityFilterChain filterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable());
            return http.build();
        }
    }

    @Test
    @DisplayName("generateVerifyCode - 正常发送")
    void testGenerateVerifyCode_Success() throws Exception {
        String email = "test@example.com";
        EmailRequest req = new EmailRequest();
        req.setEmail(email);
        // mock service 正常
        ResultActions result = mockMvc.perform(post("/api/auth/generate-verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("generateVerifyCode - 邮箱为空")
    void testGenerateVerifyCode_EmailEmpty() throws Exception {
        EmailRequest req = new EmailRequest();
        req.setEmail("");
        mockMvc.perform(post("/api/auth/generate-verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("generateVerifyCode - 邮箱格式错误")
    void testGenerateVerifyCode_EmailFormatError() throws Exception {
        EmailRequest req = new EmailRequest();
        req.setEmail("not-an-email");
        mockMvc.perform(post("/api/auth/generate-verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("generateVerifyCode - 业务异常")
    void testGenerateVerifyCode_VerificationCodeException() throws Exception {
        String email = "test@example.com";
        EmailRequest req = new EmailRequest();
        req.setEmail(email);
        doThrow(new VerificationCodeException("发送失败")).when(authService).generateAndSendVerificationCode(email);
        mockMvc.perform(post("/api/auth/generate-verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4001));
    }

    @Test
    @DisplayName("generateVerifyCode - 系统异常")
    void testGenerateVerifyCode_Exception() throws Exception {
        String email = "test@example.com";
        EmailRequest req = new EmailRequest();
        req.setEmail(email);
        doThrow(new RuntimeException("系统异常")).when(authService).generateAndSendVerificationCode(email);
        mockMvc.perform(post("/api/auth/generate-verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(5000));
    }

    @Test
    @DisplayName("verifyCode - 正常校验")
    void testVerifyCode_Success() throws Exception {
        VerifyCoderequest req = new VerifyCoderequest();
        req.setCode("123456");
        req.setEmail("test@example.com");
        mockMvc.perform(post("/api/auth/verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("verifyCode - 缺少参数")
    void testVerifyCode_ParamMissing() throws Exception {
        VerifyCoderequest req = new VerifyCoderequest();
        req.setCode("");
        req.setEmail("");
        mockMvc.perform(post("/api/auth/verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("verifyCode - 业务异常")
    void testVerifyCode_VerificationCodeException() throws Exception {
        VerifyCoderequest req = new VerifyCoderequest();
        req.setCode("123456");
        req.setEmail("test@example.com");
        doThrow(new VerificationCodeException("验证码错误")).when(authService).verifyCode("123456", "test@example.com");
        mockMvc.perform(post("/api/auth/verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4001));
    }

    @Test
    @DisplayName("verifyCode - 系统异常")
    void testVerifyCode_Exception() throws Exception {
        VerifyCoderequest req = new VerifyCoderequest();
        req.setCode("123456");
        req.setEmail("test@example.com");
        doThrow(new RuntimeException("系统异常")).when(authService).verifyCode(anyString(), anyString());
        mockMvc.perform(post("/api/auth/verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(5000));
    }

    @Test
    @DisplayName("registerIndividual - 正常注册")
    void testRegisterIndividual_Success() throws Exception {
        IndividualRegisterRequest req = new IndividualRegisterRequest();
        req.setEmail("test@example.com");
        mockMvc.perform(post("/api/auth/register/individual")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("registerIndividual - 邮箱为空")
    void testRegisterIndividual_EmailEmpty() throws Exception {
        IndividualRegisterRequest req = new IndividualRegisterRequest();
        req.setEmail("");
        mockMvc.perform(post("/api/auth/register/individual")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("registerIndividual - 邮箱格式错误")
    void testRegisterIndividual_EmailFormatError() throws Exception {
        IndividualRegisterRequest req = new IndividualRegisterRequest();
        req.setEmail("not-an-email");
        mockMvc.perform(post("/api/auth/register/individual")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("registerIndividual - 业务异常")
    void testRegisterIndividual_VerificationCodeException() throws Exception {
        IndividualRegisterRequest req = new IndividualRegisterRequest();
        req.setEmail("test@example.com");
        doThrow(new VerificationCodeException("注册失败")).when(authService).registerIndividual(any());
        mockMvc.perform(post("/api/auth/register/individual")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4001));
    }

    @Test
    @DisplayName("registerIndividual - 系统异常")
    void testRegisterIndividual_Exception() throws Exception {
        IndividualRegisterRequest req = new IndividualRegisterRequest();
        req.setEmail("test@example.com");
        doThrow(new RuntimeException("系统异常")).when(authService).registerIndividual(any());
        mockMvc.perform(post("/api/auth/register/individual")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(5000));
    }

    @Test
    @DisplayName("registerEnterprise - 正常注册")
    void testRegisterEnterprise_Success() throws Exception {
        EnterpriseRegisterRequest req = new EnterpriseRegisterRequest();
        mockMvc.perform(post("/api/auth/register/enterprise")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("registerEnterprise - 业务异常")
    void testRegisterEnterprise_VerificationCodeException() throws Exception {
        EnterpriseRegisterRequest req = new EnterpriseRegisterRequest();
        doThrow(new VerificationCodeException("注册失败")).when(authService).registerEnterprise(any());
        mockMvc.perform(post("/api/auth/register/enterprise")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4001));
    }

    @Test
    @DisplayName("registerEnterprise - 系统异常")
    void testRegisterEnterprise_Exception() throws Exception {
        EnterpriseRegisterRequest req = new EnterpriseRegisterRequest();
        doThrow(new RuntimeException("系统异常")).when(authService).registerEnterprise(any());
        mockMvc.perform(post("/api/auth/register/enterprise")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(5000));
    }

    @Test
    @DisplayName("userLogin - 正常登录")
    void testUserLogin_Success() throws Exception {
        UserLoginRequest req = new UserLoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("123456");
        LoginResponse loginResponse = new LoginResponse();
        when(authService.userLogin(any())).thenReturn(loginResponse);
        mockMvc.perform(post("/api/auth/login/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("userLogin - 账号或密码错误")
    void testUserLogin_BadCredentials() throws Exception {
        UserLoginRequest req = new UserLoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("wrong");
        when(authService.userLogin(any())).thenThrow(new BadCredentialsException("账号或密码错误"));
        mockMvc.perform(post("/api/auth/login/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("userLogin - 系统异常")
    void testUserLogin_Exception() throws Exception {
        UserLoginRequest req = new UserLoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("123456");
        when(authService.userLogin(any())).thenThrow(new RuntimeException("系统异常"));
        mockMvc.perform(post("/api/auth/login/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(5000));
    }

    @Test
    @DisplayName("companyLogin - 正常登录")
    void testCompanyLogin_Success() throws Exception {
        CompanyLoginrequest req = new CompanyLoginrequest();
        LoginResponse loginResponse = new LoginResponse();
        when(authService.companyLogin(any())).thenReturn(loginResponse);
        mockMvc.perform(post("/api/auth/login/company")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("companyLogin - 账号或密码错误")
    void testCompanyLogin_BadCredentials() throws Exception {
        CompanyLoginrequest req = new CompanyLoginrequest();
        when(authService.companyLogin(any())).thenThrow(new BadCredentialsException("账号或密码错误"));
        mockMvc.perform(post("/api/auth/login/company")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("companyLogin - 系统异常")
    void testCompanyLogin_Exception() throws Exception {
        CompanyLoginrequest req = new CompanyLoginrequest();
        when(authService.companyLogin(any())).thenThrow(new RuntimeException("系统异常"));
        mockMvc.perform(post("/api/auth/login/company")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(5000));
    }

    @Test
    @DisplayName("logout - 正常登出")
    void testLogout_Success() throws Exception {
        RefreshTokenRequest req = new RefreshTokenRequest();
        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("refreshToken - 正常刷新")
    void testRefreshToken_Success() throws Exception {
        RefreshTokenRequest req = new RefreshTokenRequest();
        LoginResponse loginResponse = new LoginResponse();
        when(authService.refreshToken(any())).thenReturn(loginResponse);
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("refreshToken - 账号或密码错误")
    void testRefreshToken_BadCredentials() throws Exception {
        RefreshTokenRequest req = new RefreshTokenRequest();
        when(authService.refreshToken(any())).thenThrow(new BadCredentialsException("token无效"));
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("refreshToken - 系统异常")
    void testRefreshToken_Exception() throws Exception {
        RefreshTokenRequest req = new RefreshTokenRequest();
        when(authService.refreshToken(any())).thenThrow(new RuntimeException("系统异常"));
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(5000));
    }
} 