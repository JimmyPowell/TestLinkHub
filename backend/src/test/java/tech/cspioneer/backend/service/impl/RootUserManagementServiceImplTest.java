package tech.cspioneer.backend.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.entity.dto.request.RootUserSearchRequest;
import tech.cspioneer.backend.entity.dto.request.UserCreateRequest;
import tech.cspioneer.backend.entity.dto.request.UserUpdateRequest;
import tech.cspioneer.backend.entity.dto.response.UserResponse;
import tech.cspioneer.backend.exception.ResourceNotFoundException;
import tech.cspioneer.backend.exception.UserManagementException;
import tech.cspioneer.backend.mapper.RootUserManagementMapper;
import tech.cspioneer.backend.mapper.UserMapper;
import tech.cspioneer.backend.model.response.PagedResponse;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RootUserManagementServiceImplTest {

    @Mock RootUserManagementMapper rootUserManagementMapper;
    @Mock UserMapper userMapper;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks RootUserManagementServiceImpl service;

    @BeforeEach
    void setUp() {
        reset(rootUserManagementMapper, userMapper, passwordEncoder);
    }

    @Test
    void testGetAllUsers_Normal() {
        RootUserSearchRequest req = new RootUserSearchRequest();
        List<UserResponse> users = Collections.singletonList(new UserResponse());
        when(rootUserManagementMapper.findUsers(eq(req), anyInt(), anyInt())).thenReturn(users);
        when(rootUserManagementMapper.countUsers(eq(req))).thenReturn(10L);
        PagedResponse<UserResponse> resp = service.getAllUsers(0, 10, req);
        assertEquals(1, resp.getContent().size());
        assertEquals(10, resp.getTotalElements());
        assertTrue(resp.isLast());
    }

    @Test
    void testGetAllUsers_Empty() {
        RootUserSearchRequest req = new RootUserSearchRequest();
        when(rootUserManagementMapper.findUsers(eq(req), anyInt(), anyInt())).thenReturn(Collections.emptyList());
        when(rootUserManagementMapper.countUsers(eq(req))).thenReturn(0L);
        PagedResponse<UserResponse> resp = service.getAllUsers(0, 10, req);
        assertTrue(resp.getContent().isEmpty());
        assertEquals(0, resp.getTotalElements());
        assertTrue(resp.isLast());
    }

    @Test
    void testGetUserById_Success() {
        User user = new User();
        user.setId(1L);
        user.setUuid("uuid");
        when(userMapper.findById(1L)).thenReturn(user);
        UserResponse resp = service.getUserById(1L);
        assertEquals("uuid", resp.getUuid());
    }

    @Test
    void testGetUserById_NotFound() {
        when(userMapper.findById(1L)).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> service.getUserById(1L));
    }

    @Test
    void testGetUserByUuid_Success() {
        User user = new User();
        user.setUuid("uuid");
        when(userMapper.findByUuid("uuid")).thenReturn(user);
        UserResponse resp = service.getUserByUuid("uuid");
        assertEquals("uuid", resp.getUuid());
    }

    @Test
    void testGetUserByUuid_NotFound() {
        when(userMapper.findByUuid("uuid")).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> service.getUserByUuid("uuid"));
    }

    @Test
    void testCreateUser_Success() {
        UserCreateRequest req = new UserCreateRequest();
        req.setEmail("test@test.com");
        req.setPassword("123456");
        when(userMapper.findByEmail("test@test.com")).thenReturn(null);
        when(passwordEncoder.encode("123456")).thenReturn("encoded");
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return null;
        }).when(userMapper).insert(any(User.class));
        UserResponse resp = service.createUser(req);
        assertEquals("test@test.com", resp.getEmail());
    }

    @Test
    void testCreateUser_EmailExists() {
        UserCreateRequest req = new UserCreateRequest();
        req.setEmail("test@test.com");
        when(userMapper.findByEmail("test@test.com")).thenReturn(new User());
        assertThrows(UserManagementException.class, () -> service.createUser(req));
    }

    @Test
    void testUpdateUser_Success() {
        String uuid = "uuid";
        UserUpdateRequest req = new UserUpdateRequest();
        User user = new User();
        user.setUuid(uuid);
        when(userMapper.findByUuid(uuid)).thenReturn(user);
        when(userMapper.update(any(User.class))).thenReturn(1);
        UserResponse resp = service.updateUser(uuid, req);
        assertEquals(uuid, resp.getUuid());
    }

    @Test
    void testUpdateUser_NotFound() {
        String uuid = "uuid";
        UserUpdateRequest req = new UserUpdateRequest();
        when(userMapper.findByUuid(uuid)).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> service.updateUser(uuid, req));
    }

    @Test
    void testDeleteUser_Success() {
        String uuid = "uuid";
        User user = new User();
        user.setUuid(uuid);
        when(userMapper.findByUuid(uuid)).thenReturn(user);
        when(userMapper.deleteByUuid(uuid)).thenReturn(1);
        service.deleteUser(uuid);
        verify(userMapper, times(1)).deleteByUuid(uuid);
    }

    @Test
    void testDeleteUser_NotFound() {
        String uuid = "uuid";
        when(userMapper.findByUuid(uuid)).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> service.deleteUser(uuid));
    }
} 