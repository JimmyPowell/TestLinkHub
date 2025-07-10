package tech.cspioneer.backend.service;

import tech.cspioneer.backend.entity.dto.request.UserCreateRequest;
import tech.cspioneer.backend.entity.dto.request.UserUpdateRequest;
import tech.cspioneer.backend.entity.dto.response.UserResponse;
import tech.cspioneer.backend.model.response.PagedResponse;

public interface AdminUserManagementService {

    PagedResponse<UserResponse> getAllUsers(int page, int size);

    UserResponse getUserByUuid(String uuid);

    UserResponse createUser(UserCreateRequest userCreateRequest);

    UserResponse updateUser(String uuid, UserUpdateRequest userUpdateRequest);

    void deleteUser(String uuid);
}
