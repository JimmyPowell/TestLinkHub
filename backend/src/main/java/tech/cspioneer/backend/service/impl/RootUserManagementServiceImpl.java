package tech.cspioneer.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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
import tech.cspioneer.backend.service.RootUserManagementService;
import tech.cspioneer.backend.utils.UuidUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RootUserManagementServiceImpl implements RootUserManagementService {

    private final RootUserManagementMapper rootUserManagementMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PagedResponse<UserResponse> getAllUsers(int page, int size, RootUserSearchRequest request) {
        int offset = page * size;
        List<UserResponse> users = rootUserManagementMapper.findUsers(request, offset, size);
        long totalElements = rootUserManagementMapper.countUsers(request);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean isLast = (page + 1) * size >= totalElements;
        return new PagedResponse<>(users, page, size, totalElements, totalPages, isLast);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = Optional.ofNullable(userMapper.findById(id))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        UserResponse userResponse = new UserResponse();
        BeanUtils.copyProperties(user, userResponse);
        return userResponse;
    }

    @Override
    public UserResponse getUserByUuid(String uuid) {
        User user = Optional.ofNullable(userMapper.findByUuid(uuid))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UUID: " + uuid));
        UserResponse userResponse = new UserResponse();
        BeanUtils.copyProperties(user, userResponse);
        return userResponse;
    }

    @Override
    public UserResponse createUser(UserCreateRequest userCreateRequest) {
        if (userMapper.findByEmail(userCreateRequest.getEmail()) != null) {
            throw new UserManagementException("Email is already in use.");
        }

        User user = new User();
        BeanUtils.copyProperties(userCreateRequest, user);
        user.setUuid(UuidUtils.randomUuid());
        user.setPassword(passwordEncoder.encode(userCreateRequest.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);

        UserResponse userResponse = new UserResponse();
        BeanUtils.copyProperties(user, userResponse);
        return userResponse;
    }

    @Override
    public UserResponse updateUser(String uuid, UserUpdateRequest userUpdateRequest) {
        User user = Optional.ofNullable(userMapper.findByUuid(uuid))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UUID: " + uuid));
        
        // No need to check email as UserUpdateRequest doesn't contain email field
        
        BeanUtils.copyProperties(userUpdateRequest, user, "id", "uuid", "password", "createdAt");
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.update(user);

        UserResponse userResponse = new UserResponse();
        BeanUtils.copyProperties(user, userResponse);
        return userResponse;
    }

    @Override
    public void deleteUser(String uuid) {
        User user = Optional.ofNullable(userMapper.findByUuid(uuid))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UUID: " + uuid));
        userMapper.deleteByUuid(uuid);
    }
}
