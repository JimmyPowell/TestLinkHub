package tech.cspioneer.backend.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.entity.dto.request.UserCreateRequest;
import tech.cspioneer.backend.entity.dto.request.UserUpdateRequest;
import tech.cspioneer.backend.entity.dto.response.UserResponse;
import tech.cspioneer.backend.exception.ResourceNotFoundException;
import tech.cspioneer.backend.exception.UserManagementException;
import tech.cspioneer.backend.mapper.UserMapper;
import tech.cspioneer.backend.model.response.PagedResponse;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdminUserManagementServiceImplTest {
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AdminUserManagementServiceImpl adminUserManagementService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllUsers_shouldReturnPagedResponse() {
        User user = new User();
        user.setUuid(UUID.randomUUID().toString());
        when(userMapper.findUsersWithPagination(anyInt(), anyInt())).thenReturn(Collections.singletonList(user));
        when(userMapper.countAllUsers()).thenReturn(1L);

        PagedResponse<UserResponse> response = adminUserManagementService.getAllUsers(0, 10);
        assertEquals(1, response.getContent().size());
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void getUserByUuid_shouldReturnUserResponse() {
        User user = new User();
        user.setUuid("test-uuid");
        when(userMapper.findByUuid("test-uuid")).thenReturn(user);
        UserResponse response = adminUserManagementService.getUserByUuid("test-uuid");
        assertEquals("test-uuid", response.getUuid());
    }

    @Test
    void getUserByUuid_shouldThrowExceptionIfNotFound() {
        when(userMapper.findByUuid("not-exist")).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> adminUserManagementService.getUserByUuid("not-exist"));
    }

    @Test
    void createUser_shouldCreateAndReturnUser() {
        UserCreateRequest req = new UserCreateRequest();
        req.setEmail("test@example.com");
        when(userMapper.findByEmail("test@example.com")).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenReturn(1);
        UserResponse response = adminUserManagementService.createUser(req);
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    void createUser_shouldThrowExceptionIfEmailExists() {
        UserCreateRequest req = new UserCreateRequest();
        req.setEmail("exist@example.com");
        when(userMapper.findByEmail("exist@example.com")).thenReturn(new User());
        assertThrows(UserManagementException.class, () -> adminUserManagementService.createUser(req));
    }

    @Test
    void updateUser_shouldUpdateAndReturnUser() {
        User user = new User();
        user.setUuid("update-uuid");
        when(userMapper.findByUuid("update-uuid")).thenReturn(user);
        when(userMapper.update(any(User.class))).thenReturn(1);
        UserUpdateRequest req = new UserUpdateRequest();
        UserResponse response = adminUserManagementService.updateUser("update-uuid", req);
        assertEquals("update-uuid", response.getUuid());
    }

    @Test
    void updateUser_shouldThrowExceptionIfNotFound() {
        when(userMapper.findByUuid("not-exist")).thenReturn(null);
        UserUpdateRequest req = new UserUpdateRequest();
        assertThrows(ResourceNotFoundException.class, () -> adminUserManagementService.updateUser("not-exist", req));
    }

    @Test
    void deleteUser_shouldDeleteUser() {
        User user = new User();
        user.setUuid("delete-uuid");
        when(userMapper.findByUuid("delete-uuid")).thenReturn(user);
        when(userMapper.deleteByUuid("delete-uuid")).thenReturn(1);
        assertDoesNotThrow(() -> adminUserManagementService.deleteUser("delete-uuid"));
    }

    @Test
    void deleteUser_shouldThrowExceptionIfNotFound() {
        when(userMapper.findByUuid("not-exist")).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> adminUserManagementService.deleteUser("not-exist"));
    }
} 