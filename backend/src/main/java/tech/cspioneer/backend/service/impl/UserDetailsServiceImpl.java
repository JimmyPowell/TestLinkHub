package tech.cspioneer.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.mapper.UserMapper;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // In our system, the "username" is the user's UUID.
        User user = userMapper.findByUuid(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with uuid: " + username);
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUuid(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }
}
