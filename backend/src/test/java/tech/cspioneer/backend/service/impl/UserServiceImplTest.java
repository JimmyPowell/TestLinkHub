package tech.cspioneer.backend.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.cspioneer.backend.entity.Company;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.entity.dto.request.UpdateUserRequest;
import tech.cspioneer.backend.entity.dto.response.CurrentUserResponse;
import tech.cspioneer.backend.entity.dto.response.UserInfoResponse;
import tech.cspioneer.backend.entity.enums.Gender;
import tech.cspioneer.backend.entity.enums.UserStatus;
import tech.cspioneer.backend.exception.ResourceNotFoundException;
import tech.cspioneer.backend.mapper.CompanyMapper;
import tech.cspioneer.backend.mapper.UserMapper;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock UserMapper userMapper;
    @Mock CompanyMapper companyMapper;
    @InjectMocks UserServiceImpl service;

    @BeforeEach
    void setUp() {
        reset(userMapper, companyMapper);
    }

    @Test
    void testGetCurrentUserInfo_Company_Success() {
        String uuid = "company-uuid";
        Company company = new Company();
        company.setUuid(uuid);
        company.setName("TestCompany");
        when(companyMapper.findByUuid(uuid)).thenReturn(company);

        CurrentUserResponse resp = service.getCurrentUserInfo(uuid, "COMPANY");
        assertEquals("TestCompany", resp.getName());
        assertEquals("COMPANY", resp.getRole());
    }

    @Test
    void testGetCurrentUserInfo_Company_NotFound() {
        String uuid = "company-uuid";
        when(companyMapper.findByUuid(uuid)).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> service.getCurrentUserInfo(uuid, "COMPANY"));
    }

    @Test
    void testGetCurrentUserInfo_User_Success() {
        String uuid = "user-uuid";
        User user = new User();
        user.setUuid(uuid);
        user.setName("TestUser");
        when(userMapper.findByUuid(uuid)).thenReturn(user);

        CurrentUserResponse resp = service.getCurrentUserInfo(uuid, "USER");
        assertEquals("TestUser", resp.getName());
        assertEquals("USER", resp.getRole());
    }

    @Test
    void testGetCurrentUserInfo_User_NotFound() {
        String uuid = "user-uuid";
        when(userMapper.findByUuid(uuid)).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> service.getCurrentUserInfo(uuid, "USER"));
    }

    @Test
    void testGetUserInfo_Success() {
        String uuid = "user-uuid";
        User user = new User();
        user.setUuid(uuid);
        user.setName("TestUser");
        user.setEmail("test@test.com");
        user.setPhoneNumber("123456");
        user.setAddress("addr");
        user.setAvatarUrl("avatar");
        user.setGender(Gender.MALE);
        user.setCompanyId(1L);
        user.setStatus(UserStatus.ACTIVE);
        user.setDescription("desc");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        Company company = new Company();
        company.setId(1L);
        company.setName("TestCompany");

        when(userMapper.findByUuid(uuid)).thenReturn(user);
        when(companyMapper.findById(1L)).thenReturn(company);

        UserInfoResponse resp = service.getUserInfo(uuid);
        assertEquals("TestUser", resp.getName());
        assertEquals("TestCompany", resp.getCompanyName());
    }

    @Test
    void testGetUserInfo_NotFound() {
        String uuid = "user-uuid";
        when(userMapper.findByUuid(uuid)).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> service.getUserInfo(uuid));
    }

    @Test
    void testUpdateUserInfo_Success() {
        String uuid = "user-uuid";
        User user = new User();
        user.setUuid(uuid);
        user.setCompanyId(1L);

        UpdateUserRequest req = new UpdateUserRequest();
        req.setName("NewName");
        req.setPhoneNumber("654321");
        req.setAddress("newAddr");
        req.setAvatarUrl("newAvatar");
        req.setGender("male");
        req.setDescription("newDesc");

        Company company = new Company();
        company.setId(1L);
        company.setName("TestCompany");

        when(userMapper.findByUuid(uuid)).thenReturn(user);
        when(userMapper.update(any(User.class))).thenReturn(1);
        when(companyMapper.findById(1L)).thenReturn(company);

        UserInfoResponse resp = service.updateUserInfo(uuid, req);
        assertEquals("NewName", resp.getName());
        assertEquals("TestCompany", resp.getCompanyName());
        assertEquals(Gender.MALE, resp.getGender());
        assertEquals("newDesc", resp.getDescription());
    }

    @Test
    void testUpdateUserInfo_NotFound() {
        String uuid = "user-uuid";
        UpdateUserRequest req = new UpdateUserRequest();
        when(userMapper.findByUuid(uuid)).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> service.updateUserInfo(uuid, req));
    }
} 