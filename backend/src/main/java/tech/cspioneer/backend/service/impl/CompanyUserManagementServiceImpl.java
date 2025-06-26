package tech.cspioneer.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import tech.cspioneer.backend.entity.Company;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.entity.dto.request.UserCreateRequest;
import tech.cspioneer.backend.entity.dto.request.UserUpdateRequest;
import tech.cspioneer.backend.entity.dto.response.UserResponse;
import tech.cspioneer.backend.exception.ResourceNotFoundException;
import tech.cspioneer.backend.exception.UserManagementException;
import tech.cspioneer.backend.mapper.CompanyMapper;
import tech.cspioneer.backend.mapper.UserMapper;
import tech.cspioneer.backend.model.response.PagedResponse;
import tech.cspioneer.backend.service.CompanyUserManagementService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyUserManagementServiceImpl implements CompanyUserManagementService {

    private final UserMapper userMapper;
    private final CompanyMapper companyMapper;

    @Override
    public PagedResponse<UserResponse> getCompanyUsers(int page, int size, String currentUserUuid) {
        Company company = getCompanyFromCurrentUser(currentUserUuid);
        int offset = page * size;
        List<User> users = userMapper.findUsersWithPaginationByCompanyId(company.getId(), offset, size);
        long totalElements = userMapper.countAllUsersByCompanyId(company.getId());
        int totalPages = (int) Math.ceil((double) totalElements / size);

        List<UserResponse> userResponses = users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(userResponses, page, size, totalElements, totalPages, page >= totalPages - 1);
    }

    @Override
    public UserResponse createCompanyUser(UserCreateRequest userCreateRequest, String currentUserUuid) {
        if (userMapper.findByEmail(userCreateRequest.getEmail()) != null) {
            throw new UserManagementException("Email already in use: " + userCreateRequest.getEmail());
        }

        Company company = getCompanyFromCurrentUser(currentUserUuid);
        userCreateRequest.setCompanyId(company.getId());

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
    public UserResponse updateCompanyUser(String uuid, UserUpdateRequest userUpdateRequest, String currentUserUuid) {
        User user = findUserAndCheckPermission(uuid, currentUserUuid);
        
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
    public void deleteCompanyUser(String uuid, String currentUserUuid) {
        User user = findUserAndCheckPermission(uuid, currentUserUuid);
        userMapper.deleteByUuid(uuid);
    }

    @Override
    public void removeUserFromCompany(String uuid, String currentUserUuid) {
        User user = findUserAndCheckPermission(uuid, currentUserUuid);
        userMapper.removeUserFromCompany(uuid);
    }

    private User findUserAndCheckPermission(String userUuid, String currentUserUuid) {
        User user = userMapper.findByUuid(userUuid);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with uuid: " + userUuid);
        }

        Company company = getCompanyFromCurrentUser(currentUserUuid);
        if (user.getCompanyId() == null || !user.getCompanyId().equals(company.getId())) {
            throw new AccessDeniedException("Access Denied: You do not have permission to manage this user.");
        }
        return user;
    }

    private Company getCompanyFromCurrentUser(String companyUuid) {
        Company company = companyMapper.findByUuid(companyUuid);
        if (company == null) {
            throw new ResourceNotFoundException("Company not found for current user with uuid: " + companyUuid);
        }
        return company;
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
