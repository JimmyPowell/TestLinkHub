package tech.cspioneer.backend.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.entity.enums.UserRole;
import tech.cspioneer.backend.mapper.UserMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    UserMapper userMapper;

    @InjectMocks
    UserDetailsServiceImpl service;

    @BeforeEach
    void setUp() {
        reset(userMapper);
    }

    @Test
    void testLoadUserByUsername_Success() {
        String uuid = "test-uuid";
        String password = "encoded-password";
        User user = new User();
        user.setUuid(uuid);
        user.setPassword(password);
        user.setRole(UserRole.ADMIN);

        when(userMapper.findByUuid(uuid)).thenReturn(user);

        UserDetails userDetails = service.loadUserByUsername(uuid);

        assertEquals(uuid, userDetails.getUsername());
        assertEquals(password, userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN")));
    }

    @Test
    void testLoadUserByUsername_NotFound() {
        String uuid = "not-exist";
        when(userMapper.findByUuid(uuid)).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername(uuid));
    }
} 