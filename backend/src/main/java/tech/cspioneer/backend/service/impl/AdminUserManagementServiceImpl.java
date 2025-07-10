package tech.cspioneer.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.entity.dto.request.UserCreateRequest;
import tech.cspioneer.backend.entity.dto.request.UserUpdateRequest;
import tech.cspioneer.backend.entity.dto.response.UserResponse;
import tech.cspioneer.backend.exception.ResourceNotFoundException;
import tech.cspioneer.backend.exception.UserManagementException;
import tech.cspioneer.backend.mapper.UserMapper;
import tech.cspioneer.backend.model.response.PagedResponse;
import tech.cspioneer.backend.service.AdminUserManagementService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserManagementServiceImpl implements AdminUserManagementService {

    private final UserMapper userMapper;

    @Override
    public PagedResponse<UserResponse> getAllUsers(int page, int size) {
        int offset = page * size;
        List<User> users = userMapper.findUsersWithPagination(offset, size);
        long totalElements = userMapper.countAllUsers();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        List<UserResponse> userResponses = users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(userResponses, page, size, totalElements, totalPages, page >= totalPages - 1);
    }

    @Override
    public UserResponse getUserByUuid(String uuid) {
        User user = userMapper.findByUuid(uuid);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with uuid: " + uuid);
        }
        return convertToUserResponse(user);
    }

    @Override
    public UserResponse createUser(UserCreateRequest userCreateRequest) {
        if (userMapper.findByEmail(userCreateRequest.getEmail()) != null) {
            throw new UserManagementException("Email already in use: " + userCreateRequest.getEmail());
        }

        User user = new User();
        user.setUuid(UUID.randomUUID().toString());
        user.setName(userCreateRequest.getName());
        user.setEmail(userCreateRequest.getEmail());
        user.setPassword(userCreateRequest.getPassword()); // Should be encoded
        user.setPhoneNumber(userCreateRequest.getPhoneNumber());
        user.setAddress(userCreateRequest.getAddress());
        user.setAvatarUrl(userCreateRequest.getAvatarUrl());
        user.setGender(userCreateRequest.getGender());
        user.setCompanyId(userCreateRequest.getCompanyId());
        user.setRole(userCreateRequest.getRole());
        user.setStatus(userCreateRequest.getStatus());
        user.setDescription(userCreateRequest.getDescription());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        userMapper.insert(user);
        return convertToUserResponse(user);
    }

    @Override
    public UserResponse updateUser(String uuid, UserUpdateRequest userUpdateRequest) {
        User user = userMapper.findByUuid(uuid);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with uuid: " + uuid);
        }
        
        user.setName(userUpdateRequest.getName());
        user.setPhoneNumber(userUpdateRequest.getPhoneNumber());
        user.setAddress(userUpdateRequest.getAddress());
        user.setAvatarUrl(userUpdateRequest.getAvatarUrl());
        user.setGender(userUpdateRequest.getGender());
        user.setCompanyId(userUpdateRequest.getCompanyId());
        user.setRole(userUpdateRequest.getRole());
        user.setStatus(userUpdateRequest.getStatus());
        user.setDescription(userUpdateRequest.getDescription());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.update(user); 
        
        return convertToUserResponse(user);
    }

    @Override
    public void deleteUser(String uuid) {
        User user = userMapper.findByUuid(uuid);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with uuid: " + uuid);
        }
        userMapper.deleteByUuid(uuid);
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setUuid(user.getUuid());
        userResponse.setName(user.getName());
        userResponse.setEmail(user.getEmail());
        userResponse.setPhoneNumber(user.getPhoneNumber());
        userResponse.setAddress(user.getAddress());
        userResponse.setAvatarUrl(user.getAvatarUrl());
        userResponse.setGender(user.getGender());
        userResponse.setCompanyId(user.getCompanyId());
        userResponse.setRole(user.getRole());
        userResponse.setStatus(user.getStatus());
        userResponse.setDescription(user.getDescription());
        userResponse.setPostCount(user.getPostCount());
        userResponse.setLessonCount(user.getLessonCount());
        userResponse.setMeetingCount(user.getMeetingCount());
        userResponse.setUpdatedAt(user.getUpdatedAt());
        return userResponse;
    }
}
