package tech.cspioneer.backend.service.impl;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import tech.cspioneer.backend.entity.Company;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.entity.dto.request.*;
import tech.cspioneer.backend.entity.dto.response.LoginResponse;
import tech.cspioneer.backend.entity.enums.CompanyStatus;
import tech.cspioneer.backend.entity.enums.UserRole;
import tech.cspioneer.backend.entity.enums.UserStatus;
import tech.cspioneer.backend.exception.VerificationCodeException;
import tech.cspioneer.backend.mapper.CompanyMapper;
import tech.cspioneer.backend.mapper.UserMapper;
import tech.cspioneer.backend.utils.JwtUtils;
import tech.cspioneer.backend.utils.RedisUtils;
import tech.cspioneer.backend.utils.SMTPUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceImplTest {
    @Mock SMTPUtils smtpUtils;
    @Mock UserMapper userMapper;
    @Mock CompanyMapper companyMapper;
    @Mock BCryptPasswordEncoder passwordEncoder;
    @Mock JwtUtils jwtUtils;
    @InjectMocks AuthServiceImpl service;
    MockedStatic<RedisUtils> redisMock;

    @BeforeEach
    void setUp() {
        redisMock = Mockito.mockStatic(RedisUtils.class);
    }

    @AfterEach
    void tearDown() {
        redisMock.close();
    }

    @Test
    void testGenerateAndSendVerificationCode_Normal() {
        when(smtpUtils.sendHtmlMail(anyString(), anyString(), anyString())).thenReturn(true);
        assertDoesNotThrow(() -> service.generateAndSendVerificationCode("test@test.com"));
    }

    @Test
    void testGenerateAndSendVerificationCode_SendFail() {
        when(smtpUtils.sendHtmlMail(anyString(), anyString(), anyString())).thenReturn(false);
        assertThrows(VerificationCodeException.class, () -> service.generateAndSendVerificationCode("test@test.com"));
    }

    @Test
    void testGenerateAndSendVerificationCode_RedisFail() {
        when(smtpUtils.sendHtmlMail(anyString(), anyString(), anyString())).thenReturn(true);
        redisMock.when(() -> RedisUtils.set(anyString(), anyString(), anyInt(), anyInt())).thenThrow(new RuntimeException());
        assertThrows(VerificationCodeException.class, () -> service.generateAndSendVerificationCode("test@test.com"));
    }

    @Test
    void testVerifyCode_Success() {
        redisMock.when(() -> RedisUtils.get(anyString(), anyInt())).thenReturn("123456");
        redisMock.when(() -> RedisUtils.del(anyString(), anyInt())).then(invocation -> null);
        redisMock.when(() -> RedisUtils.set(contains("verified:user:"), eq("true"), anyInt(), anyInt())).then(invocation -> null);
        assertDoesNotThrow(() -> service.verifyCode("123456", "test@test.com"));
    }

    @Test
    void testVerifyCode_CodeEmpty() {
        assertThrows(VerificationCodeException.class, () -> service.verifyCode("", "test@test.com"));
    }

    @Test
    void testVerifyCode_EmailEmpty() {
        assertThrows(VerificationCodeException.class, () -> service.verifyCode("123456", ""));
    }

    @Test
    void testVerifyCode_CodeNotExist() {
        redisMock.when(() -> RedisUtils.get(anyString(), anyInt())).thenReturn(null);
        assertThrows(VerificationCodeException.class, () -> service.verifyCode("123456", "test@test.com"));
    }

    @Test
    void testVerifyCode_CodeNotMatch() {
        redisMock.when(() -> RedisUtils.get(anyString(), anyInt())).thenReturn("654321");
        assertThrows(VerificationCodeException.class, () -> service.verifyCode("123456", "test@test.com"));
    }

    @Test
    void testVerifyCode_DelThrows() {
        redisMock.when(() -> RedisUtils.get(anyString(), anyInt())).thenReturn("123456");
        redisMock.when(() -> RedisUtils.del(anyString(), anyInt())).thenThrow(new RuntimeException());
        redisMock.when(() -> RedisUtils.set(contains("verified:user:"), eq("true"), anyInt(), anyInt())).then(invocation -> null);
        assertThrows(VerificationCodeException.class, () -> service.verifyCode("123456", "test@test.com"));
    }

    @Test
    void testVerifyCode_SetThrows() {
        redisMock.when(() -> RedisUtils.get(anyString(), anyInt())).thenReturn("123456");
        redisMock.when(() -> RedisUtils.del(anyString(), anyInt())).then(invocation -> null);
        redisMock.when(() -> RedisUtils.set(contains("verified:user:"), eq("true"), anyInt(), anyInt())).thenThrow(new RuntimeException());
        assertThrows(VerificationCodeException.class, () -> service.verifyCode("123456", "test@test.com"));
    }

    @Test
    void testVerifyCode_CatchOtherException() {
        redisMock.when(() -> RedisUtils.get(anyString(), anyInt())).thenThrow(new RuntimeException());
        assertThrows(VerificationCodeException.class, () -> service.verifyCode("123456", "test@test.com"));
    }

    @Test
    void testRegisterIndividual_Success() {
        IndividualRegisterRequest req = new IndividualRegisterRequest();
        req.setEmail("test@test.com");
        req.setPassword("pwd");
        req.setName("test");
        redisMock.when(() -> RedisUtils.get(contains("verified:user:"), anyInt())).thenReturn("true");
        when(userMapper.findByEmail(anyString())).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userMapper.insert(any(User.class))).thenReturn(1);
        assertDoesNotThrow(() -> service.registerIndividual(req));
    }

    @Test
    void testRegisterIndividual_EmailNotVerified() {
        IndividualRegisterRequest req = new IndividualRegisterRequest();
        req.setEmail("test@test.com");
        req.setPassword("pwd");
        req.setName("test");
        redisMock.when(() -> RedisUtils.get(contains("verified:user:"), anyInt())).thenReturn(null);
        assertThrows(VerificationCodeException.class, () -> service.registerIndividual(req));
    }

    @Test
    void testRegisterIndividual_EmailExists() {
        IndividualRegisterRequest req = new IndividualRegisterRequest();
        req.setEmail("test@test.com");
        req.setPassword("pwd");
        req.setName("test");
        redisMock.when(() -> RedisUtils.get(contains("verified:user:"), anyInt())).thenReturn("true");
        when(userMapper.findByEmail(anyString())).thenReturn(new User());
        assertThrows(VerificationCodeException.class, () -> service.registerIndividual(req));
    }

    @Test
    void testRegisterIndividual_ParamMissing() {
        IndividualRegisterRequest req = new IndividualRegisterRequest();
        assertThrows(VerificationCodeException.class, () -> service.registerIndividual(req));
    }

    @Test
    void testRegisterIndividual_EmailNull() {
        IndividualRegisterRequest req = new IndividualRegisterRequest();
        req.setPassword("pwd"); req.setName("test");
        assertThrows(VerificationCodeException.class, () -> service.registerIndividual(req));
    }

    @Test
    void testRegisterIndividual_PasswordNull() {
        IndividualRegisterRequest req = new IndividualRegisterRequest();
        req.setEmail("test@test.com"); req.setName("test");
        assertThrows(VerificationCodeException.class, () -> service.registerIndividual(req));
    }

    @Test
    void testRegisterIndividual_NameNull() {
        IndividualRegisterRequest req = new IndividualRegisterRequest();
        req.setEmail("test@test.com"); req.setPassword("pwd");
        assertThrows(VerificationCodeException.class, () -> service.registerIndividual(req));
    }

    @Test
    void testRegisterIndividual_EmailNotVerified2() {
        IndividualRegisterRequest req = new IndividualRegisterRequest();
        req.setEmail("test@test.com"); req.setPassword("pwd"); req.setName("test");
        redisMock.when(() -> RedisUtils.get(contains("verified:user:"), anyInt())).thenReturn(null);
        assertThrows(VerificationCodeException.class, () -> service.registerIndividual(req));
    }

    @Test
    void testRegisterIndividual_EmailVerifiedButRegistered() {
        IndividualRegisterRequest req = new IndividualRegisterRequest();
        req.setEmail("test@test.com"); req.setPassword("pwd"); req.setName("test");
        redisMock.when(() -> RedisUtils.get(contains("verified:user:"), anyInt())).thenReturn("true");
        when(userMapper.findByEmail(anyString())).thenReturn(new User());
        assertThrows(VerificationCodeException.class, () -> service.registerIndividual(req));
    }

    @Test
    void testRegisterIndividual_InsertFail() {
        IndividualRegisterRequest req = new IndividualRegisterRequest();
        req.setEmail("test@test.com"); req.setPassword("pwd"); req.setName("test");
        redisMock.when(() -> RedisUtils.get(contains("verified:user:"), anyInt())).thenReturn("true");
        when(userMapper.findByEmail(anyString())).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userMapper.insert(any(User.class))).thenReturn(0);
        assertThrows(VerificationCodeException.class, () -> service.registerIndividual(req));
    }

    @Test
    void testRegisterIndividual_InsertException() {
        IndividualRegisterRequest req = new IndividualRegisterRequest();
        req.setEmail("test@test.com"); req.setPassword("pwd"); req.setName("test");
        redisMock.when(() -> RedisUtils.get(contains("verified:user:"), anyInt())).thenReturn("true");
        when(userMapper.findByEmail(anyString())).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userMapper.insert(any(User.class))).thenThrow(new RuntimeException());
        assertThrows(VerificationCodeException.class, () -> service.registerIndividual(req));
    }

    @Test
    void testRegisterEnterprise_Success() {
        EnterpriseRegisterRequest req = new EnterpriseRegisterRequest();
        req.setEmail("test@test.com");
        req.setName("company");
        req.setCompanyCode("code");
        redisMock.when(() -> RedisUtils.get(contains("verified:user:"), anyInt())).thenReturn("true");
        when(userMapper.findByEmail(anyString())).thenReturn(null);
        when(companyMapper.findByCompanyCode(anyString())).thenReturn(null);
        when(companyMapper.findByEmail(anyString())).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(companyMapper.insert(any(Company.class))).thenReturn(1);
        assertDoesNotThrow(() -> service.registerEnterprise(req));
    }

    @Test
    void testRegisterEnterprise_EmailNotVerified() {
        EnterpriseRegisterRequest req = new EnterpriseRegisterRequest();
        req.setEmail("test@test.com");
        req.setName("company");
        req.setCompanyCode("code");
        redisMock.when(() -> RedisUtils.get(contains("verified:user:"), anyInt())).thenReturn(null);
        assertThrows(VerificationCodeException.class, () -> service.registerEnterprise(req));
    }

    @Test
    void testRegisterEnterprise_EmailExists() {
        EnterpriseRegisterRequest req = new EnterpriseRegisterRequest();
        req.setEmail("test@test.com");
        req.setName("company");
        req.setCompanyCode("code");
        redisMock.when(() -> RedisUtils.get(contains("verified:user:"), anyInt())).thenReturn("true");
        when(userMapper.findByEmail(anyString())).thenReturn(new User());
        assertThrows(VerificationCodeException.class, () -> service.registerEnterprise(req));
    }

    @Test
    void testRegisterEnterprise_CompanyCodeExists() {
        EnterpriseRegisterRequest req = new EnterpriseRegisterRequest();
        req.setEmail("test@test.com");
        req.setName("company");
        req.setCompanyCode("code");
        redisMock.when(() -> RedisUtils.get(contains("verified:user:"), anyInt())).thenReturn("true");
        when(userMapper.findByEmail(anyString())).thenReturn(null);
        when(companyMapper.findByCompanyCode(anyString())).thenReturn(new Company());
        assertThrows(VerificationCodeException.class, () -> service.registerEnterprise(req));
    }

    @Test
    void testRegisterEnterprise_CompanyEmailExists() {
        EnterpriseRegisterRequest req = new EnterpriseRegisterRequest();
        req.setEmail("test@test.com");
        req.setName("company");
        req.setCompanyCode("code");
        redisMock.when(() -> RedisUtils.get(contains("verified:user:"), anyInt())).thenReturn("true");
        when(userMapper.findByEmail(anyString())).thenReturn(null);
        when(companyMapper.findByCompanyCode(anyString())).thenReturn(null);
        when(companyMapper.findByEmail(anyString())).thenReturn(new Company());
        assertThrows(VerificationCodeException.class, () -> service.registerEnterprise(req));
    }

    @Test
    void testRegisterEnterprise_EmailNull() {
        EnterpriseRegisterRequest req = new EnterpriseRegisterRequest();
        req.setName("company"); req.setCompanyCode("code");
        assertThrows(VerificationCodeException.class, () -> service.registerEnterprise(req));
    }

    @Test
    void testRegisterEnterprise_NameNull() {
        EnterpriseRegisterRequest req = new EnterpriseRegisterRequest();
        req.setEmail("test@test.com"); req.setCompanyCode("code");
        assertThrows(VerificationCodeException.class, () -> service.registerEnterprise(req));
    }

    @Test
    void testRegisterEnterprise_CompanyCodeNull() {
        EnterpriseRegisterRequest req = new EnterpriseRegisterRequest();
        req.setEmail("test@test.com"); req.setName("company");
        assertThrows(VerificationCodeException.class, () -> service.registerEnterprise(req));
    }

    @Test
    void testRegisterEnterprise_EmailNotVerified2() {
        EnterpriseRegisterRequest req = new EnterpriseRegisterRequest();
        req.setEmail("test@test.com"); req.setName("company"); req.setCompanyCode("code");
        redisMock.when(() -> RedisUtils.get(contains("verified:user:"), anyInt())).thenReturn(null);
        assertThrows(VerificationCodeException.class, () -> service.registerEnterprise(req));
    }

    @Test
    void testRegisterEnterprise_EmailRegisteredAsUser() {
        EnterpriseRegisterRequest req = new EnterpriseRegisterRequest();
        req.setEmail("test@test.com"); req.setName("company"); req.setCompanyCode("code");
        redisMock.when(() -> RedisUtils.get(contains("verified:user:"), anyInt())).thenReturn("true");
        when(userMapper.findByEmail(anyString())).thenReturn(new User());
        assertThrows(VerificationCodeException.class, () -> service.registerEnterprise(req));
    }

    @Test
    void testRegisterEnterprise_InsertException() {
        EnterpriseRegisterRequest req = new EnterpriseRegisterRequest();
        req.setEmail("test@test.com"); req.setName("company"); req.setCompanyCode("code");
        redisMock.when(() -> RedisUtils.get(contains("verified:user:"), anyInt())).thenReturn("true");
        when(userMapper.findByEmail(anyString())).thenReturn(null);
        when(companyMapper.findByCompanyCode(anyString())).thenReturn(null);
        when(companyMapper.findByEmail(anyString())).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(companyMapper.insert(any(Company.class))).thenThrow(new RuntimeException());
        assertThrows(VerificationCodeException.class, () -> service.registerEnterprise(req));
    }

    @Test
    void testUserLogin_Success() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("encoded");
        user.setUuid("uuid");
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        when(userMapper.findByEmail(anyString())).thenReturn(user);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtils.generateAccessToken(anyString(), anyString())).thenReturn("access");
        when(jwtUtils.generateRefreshToken(anyString(), anyString(), anyInt())).thenReturn("refresh");
        redisMock.when(() -> RedisUtils.set(anyString(), anyString(), anyInt(), anyInt())).then(invocation -> null);
        UserLoginRequest req = new UserLoginRequest();
        req.setEmail("test@test.com");
        req.setPassword("pwd");
        LoginResponse resp = service.userLogin(req);
        assertEquals("access", resp.getAccessToken());
    }

    @Test
    void testUserLogin_UserNotFound() {
        when(userMapper.findByEmail(anyString())).thenReturn(null);
        UserLoginRequest req = new UserLoginRequest();
        req.setEmail("test@test.com");
        req.setPassword("pwd");
        assertThrows(BadCredentialsException.class, () -> service.userLogin(req));
    }

    @Test
    void testUserLogin_PasswordWrong() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("encoded");
        when(userMapper.findByEmail(anyString())).thenReturn(user);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        UserLoginRequest req = new UserLoginRequest();
        req.setEmail("test@test.com");
        req.setPassword("pwd");
        assertThrows(BadCredentialsException.class, () -> service.userLogin(req));
    }

    @Test
    void testUserLogin_UserNotActive() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("encoded");
        user.setStatus(UserStatus.SUSPENDED);
        when(userMapper.findByEmail(anyString())).thenReturn(user);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        UserLoginRequest req = new UserLoginRequest();
        req.setEmail("test@test.com");
        req.setPassword("pwd");
        assertThrows(BadCredentialsException.class, () -> service.userLogin(req));
    }

    @Test
    void testUserLogin_JwtException() {
        User user = new User();
        user.setEmail("test@test.com"); user.setPassword("encoded"); user.setUuid("uuid"); user.setRole(UserRole.USER); user.setStatus(UserStatus.ACTIVE);
        when(userMapper.findByEmail(anyString())).thenReturn(user);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtils.generateAccessToken(anyString(), anyString())).thenThrow(new RuntimeException());
        UserLoginRequest req = new UserLoginRequest(); req.setEmail("test@test.com"); req.setPassword("pwd");
        assertThrows(RuntimeException.class, () -> service.userLogin(req));
    }

    @Test
    void testCompanyLogin_Success() {
        Company company = new Company();
        company.setEmail("test@test.com");
        company.setPassword("encoded");
        company.setUuid("uuid");
        company.setStatus(CompanyStatus.ACTIVE);
        when(companyMapper.findByEmail(anyString())).thenReturn(company);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtils.generateAccessToken(anyString(), anyString())).thenReturn("access");
        when(jwtUtils.generateRefreshToken(anyString(), anyString(), anyInt())).thenReturn("refresh");
        redisMock.when(() -> RedisUtils.set(anyString(), anyString(), anyInt(), anyInt())).then(invocation -> null);
        CompanyLoginrequest req = new CompanyLoginrequest();
        req.setEmail("test@test.com");
        req.setPassword("pwd");
        LoginResponse resp = service.companyLogin(req);
        assertEquals("access", resp.getAccessToken());
    }

    @Test
    void testCompanyLogin_CompanyNotFound() {
        when(companyMapper.findByEmail(anyString())).thenReturn(null);
        CompanyLoginrequest req = new CompanyLoginrequest();
        req.setEmail("test@test.com");
        req.setPassword("pwd");
        assertThrows(BadCredentialsException.class, () -> service.companyLogin(req));
    }

    @Test
    void testCompanyLogin_PasswordWrong() {
        Company company = new Company();
        company.setEmail("test@test.com");
        company.setPassword("encoded");
        when(companyMapper.findByEmail(anyString())).thenReturn(company);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        CompanyLoginrequest req = new CompanyLoginrequest();
        req.setEmail("test@test.com");
        req.setPassword("pwd");
        assertThrows(BadCredentialsException.class, () -> service.companyLogin(req));
    }

    @Test
    void testCompanyLogin_CompanyNotActive() {
        Company company = new Company();
        company.setEmail("test@test.com");
        company.setPassword("encoded");
        company.setStatus(CompanyStatus.SUSPENDED);
        when(companyMapper.findByEmail(anyString())).thenReturn(company);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        CompanyLoginrequest req = new CompanyLoginrequest();
        req.setEmail("test@test.com");
        req.setPassword("pwd");
        assertThrows(BadCredentialsException.class, () -> service.companyLogin(req));
    }

    @Test
    void testCompanyLogin_JwtException() {
        Company company = new Company();
        company.setEmail("test@test.com"); company.setPassword("encoded"); company.setUuid("uuid"); company.setStatus(CompanyStatus.ACTIVE);
        when(companyMapper.findByEmail(anyString())).thenReturn(company);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtils.generateAccessToken(anyString(), anyString())).thenThrow(new RuntimeException());
        CompanyLoginrequest req = new CompanyLoginrequest(); req.setEmail("test@test.com"); req.setPassword("pwd");
        assertThrows(RuntimeException.class, () -> service.companyLogin(req));
    }

    @Test
    void testRefreshToken_TokenNull() {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken(null);
        assertThrows(BadCredentialsException.class, () -> service.refreshToken(req));
    }

    @Test
    void testRefreshToken_DecodeException() {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("token");
        when(jwtUtils.decode(anyString())).thenThrow(new RuntimeException());
        assertThrows(BadCredentialsException.class, () -> service.refreshToken(req));
    }

    @Test
    void testRefreshToken_ExpNull() {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("token");
        Map<String, Object> payload = new HashMap<>();
        when(jwtUtils.decode(anyString())).thenReturn(payload);
        assertThrows(BadCredentialsException.class, () -> service.refreshToken(req));
    }

    @Test
    void testRefreshToken_ExpExpired() {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("token");
        Map<String, Object> payload = new HashMap<>();
        payload.put("exp", System.currentTimeMillis() / 1000 - 1000);
        when(jwtUtils.decode(anyString())).thenReturn(payload);
        assertThrows(BadCredentialsException.class, () -> service.refreshToken(req));
    }

    @Test
    void testRefreshToken_UuidOrIdentityNull() {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("token");
        Map<String, Object> payload = new HashMap<>();
        payload.put("exp", System.currentTimeMillis() / 1000 + 1000);
        when(jwtUtils.decode(anyString())).thenReturn(payload);
        assertThrows(BadCredentialsException.class, () -> service.refreshToken(req));
    }

    @Test
    void testRefreshToken_RedisGetNull() {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("token");
        Map<String, Object> payload = new HashMap<>();
        payload.put("exp", System.currentTimeMillis() / 1000 + 1000);
        payload.put("uid", "uuid");
        payload.put("identity", "USER");
        payload.put("ver", 1);
        when(jwtUtils.decode(anyString())).thenReturn(payload);
        redisMock.when(() -> RedisUtils.get(anyString(), anyInt())).thenReturn(null);
        assertThrows(BadCredentialsException.class, () -> service.refreshToken(req));
    }

    @Test
    void testRefreshToken_RedisGetNotMatch() {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("token");
        Map<String, Object> payload = new HashMap<>();
        payload.put("exp", System.currentTimeMillis() / 1000 + 1000);
        payload.put("uid", "uuid");
        payload.put("identity", "USER");
        payload.put("ver", 1);
        when(jwtUtils.decode(anyString())).thenReturn(payload);
        redisMock.when(() -> RedisUtils.get(anyString(), anyInt())).thenReturn("other");
        redisMock.when(() -> RedisUtils.del(anyString(), anyInt())).then(invocation -> null);
        assertThrows(BadCredentialsException.class, () -> service.refreshToken(req));
    }

    @Test
    void testRefreshToken_Blacklisted() {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("token");
        Map<String, Object> payload = new HashMap<>();
        payload.put("exp", System.currentTimeMillis() / 1000 + 1000);
        payload.put("uid", "uuid");
        payload.put("identity", "USER");
        payload.put("ver", 1);
        when(jwtUtils.decode(anyString())).thenReturn(payload);
        redisMock.when(() -> RedisUtils.get(anyString(), anyInt())).thenReturn("token");
        redisMock.when(() -> RedisUtils.isTokenBlacklisted(anyString(), anyInt())).thenReturn(true);
        assertThrows(BadCredentialsException.class, () -> service.refreshToken(req));
    }

    @Test
    void testLogout_TokenNull() {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken(null);
        assertDoesNotThrow(() -> service.logout(req));
    }

    @Test
    void testLogout_DecodeException() {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("token");
        when(jwtUtils.decode(anyString())).thenThrow(new RuntimeException());
        assertDoesNotThrow(() -> service.logout(req));
    }

    @Test
    void testLogout_UuidOrIdentityNull() {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("token");
        Map<String, Object> payload = new HashMap<>();
        when(jwtUtils.decode(anyString())).thenReturn(payload);
        assertDoesNotThrow(() -> service.logout(req));
    }
}
