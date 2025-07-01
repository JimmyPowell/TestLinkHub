package tech.cspioneer.backend.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import tech.cspioneer.backend.entity.Company;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.entity.dto.request.UserCreateRequest;
import tech.cspioneer.backend.entity.dto.request.UserUpdateRequest;
import tech.cspioneer.backend.entity.dto.response.UserDetailResponse;
import tech.cspioneer.backend.entity.dto.response.UserResponse;
import tech.cspioneer.backend.exception.ResourceNotFoundException;
import tech.cspioneer.backend.exception.UserManagementException;
import tech.cspioneer.backend.mapper.CompanyMapper;
import tech.cspioneer.backend.mapper.CompanyUserSearchMapper;
import tech.cspioneer.backend.mapper.UserDetailMapper;
import tech.cspioneer.backend.mapper.UserMapper;
import tech.cspioneer.backend.model.response.PagedResponse;

import java.time.LocalDateTime;
import java.util.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyUserManagementServiceImplTest {
    @Mock UserMapper userMapper;
    @Mock CompanyMapper companyMapper;
    @Mock CompanyUserSearchMapper companyUserSearchMapper;
    @Mock UserDetailMapper userDetailMapper;
    @InjectMocks CompanyUserManagementServiceImpl service;

    Company mockCompany;
    User mockUser;

    @BeforeEach
    void setUp() {
        mockCompany = new Company();
        mockCompany.setId(1L);
        mockCompany.setUuid("company-uuid");
        mockUser = new User();
        mockUser.setUuid("user-uuid");
        mockUser.setCompanyId(1L);
    }

    @Test
    void testGetCompanyUsers_Normal() {
        when(companyMapper.findByUuid(anyString())).thenReturn(mockCompany);
        when(companyUserSearchMapper.searchCompanyUsers(anyLong(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(mockUser));
        when(companyUserSearchMapper.countSearchCompanyUsers(anyLong(), any(), any(), any(), any())).thenReturn(1L);
        PagedResponse<UserResponse> resp = service.getCompanyUsers(0, 10, null, null, null, null, "company-uuid");
        assertEquals(1, resp.getContent().size());
    }

    @Test
    void testGetCompanyUsers_CompanyNotFound() {
        when(companyMapper.findByUuid(anyString())).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> service.getCompanyUsers(0, 10, null, null, null, null, "company-uuid"));
    }

    @Test
    void testCreateCompanyUser_Normal() {
        when(userMapper.findByEmail(anyString())).thenReturn(null);
        when(companyMapper.findByUuid(anyString())).thenReturn(mockCompany);
        when(userMapper.insert(any(User.class))).thenReturn(1);
        UserCreateRequest req = new UserCreateRequest();
        req.setEmail("test@test.com");
        req.setName("test");
        req.setPassword("pwd");
        req.setPhoneNumber("123");
        req.setAddress("addr");
        req.setAvatarUrl("url");
        req.setGender(null);
        req.setRole(null);
        req.setStatus(null);
        req.setDescription(null);
        UserResponse resp = service.createCompanyUser(req, "company-uuid");
        assertEquals("test", resp.getName());
    }

    @Test
    void testCreateCompanyUser_EmailExists() {
        when(userMapper.findByEmail(anyString())).thenReturn(mockUser);
        UserCreateRequest req = new UserCreateRequest();
        req.setEmail("test@test.com");
        assertThrows(UserManagementException.class, () -> service.createCompanyUser(req, "company-uuid"));
    }

    @Test
    void testUpdateCompanyUser_Normal() {
        when(userMapper.findByUuid(anyString())).thenReturn(mockUser);
        when(companyMapper.findByUuid(anyString())).thenReturn(mockCompany);
        when(userMapper.update(any(User.class))).thenReturn(1);
        UserUpdateRequest req = new UserUpdateRequest();
        req.setName("new");
        req.setPhoneNumber("123");
        req.setAddress("addr");
        req.setAvatarUrl("url");
        req.setGender(null);
        req.setCompanyId(1L);
        req.setRole(null);
        req.setStatus(null);
        req.setDescription(null);
        UserResponse resp = service.updateCompanyUser("user-uuid", req, "company-uuid");
        assertEquals("new", resp.getName());
    }

    @Test
    void testUpdateCompanyUser_UserNotFound() {
        when(userMapper.findByUuid(anyString())).thenReturn(null);
        UserUpdateRequest req = new UserUpdateRequest();
        assertThrows(ResourceNotFoundException.class, () -> service.updateCompanyUser("user-uuid", req, "company-uuid"));
    }

    @Test
    void testUpdateCompanyUser_AccessDenied() {
        User other = new User();
        other.setCompanyId(2L);
        when(userMapper.findByUuid(anyString())).thenReturn(other);
        when(companyMapper.findByUuid(anyString())).thenReturn(mockCompany);
        UserUpdateRequest req = new UserUpdateRequest();
        assertThrows(AccessDeniedException.class, () -> service.updateCompanyUser("user-uuid", req, "company-uuid"));
    }

    @Test
    void testDeleteCompanyUser_Normal() {
        when(userMapper.findByUuid(anyString())).thenReturn(mockUser);
        when(companyMapper.findByUuid(anyString())).thenReturn(mockCompany);
        when(userMapper.deleteByUuid(anyString())).thenReturn(1);
        assertDoesNotThrow(() -> service.deleteCompanyUser("user-uuid", "company-uuid"));
    }

    @Test
    void testDeleteCompanyUser_UserNotFound() {
        when(userMapper.findByUuid(anyString())).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> service.deleteCompanyUser("user-uuid", "company-uuid"));
    }

    @Test
    void testRemoveUserFromCompany_Normal() {
        when(userMapper.findByUuid(anyString())).thenReturn(mockUser);
        when(companyMapper.findByUuid(anyString())).thenReturn(mockCompany);
        when(userMapper.removeUserFromCompany(anyString())).thenReturn(1);
        assertDoesNotThrow(() -> service.removeUserFromCompany("user-uuid", "company-uuid"));
    }

    @Test
    void testRemoveUserFromCompany_UserNotFound() {
        when(userMapper.findByUuid(anyString())).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> service.removeUserFromCompany("user-uuid", "company-uuid"));
    }

    @Test
    void testGetUserDetails_Normal() {
        when(userMapper.findByUuid(anyString())).thenReturn(mockUser);
        when(companyMapper.findByUuid(anyString())).thenReturn(mockCompany);
        UserDetailResponse detail = new UserDetailResponse();
        when(userDetailMapper.findUserDetailByUuid(anyString())).thenReturn(Optional.of(detail));
        UserDetailResponse resp = service.getUserDetails("user-uuid", "company-uuid");
        assertNotNull(resp);
    }

    @Test
    void testGetUserDetails_UserNotFound() {
        when(userMapper.findByUuid(anyString())).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> service.getUserDetails("user-uuid", "company-uuid"));
    }

    @Test
    void testGetUserDetails_AccessDenied() {
        User other = new User();
        other.setCompanyId(2L);
        when(userMapper.findByUuid(anyString())).thenReturn(other);
        when(companyMapper.findByUuid(anyString())).thenReturn(mockCompany);
        assertThrows(AccessDeniedException.class, () -> service.getUserDetails("user-uuid", "company-uuid"));
    }

    @Test
    void testGetUserDetails_DetailNotFound() {
        when(userMapper.findByUuid(anyString())).thenReturn(mockUser);
        when(companyMapper.findByUuid(anyString())).thenReturn(mockCompany);
        when(userDetailMapper.findUserDetailByUuid(anyString())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getUserDetails("user-uuid", "company-uuid"));
    }
}
