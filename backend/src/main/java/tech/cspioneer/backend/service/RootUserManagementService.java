package tech.cspioneer.backend.service;

import tech.cspioneer.backend.entity.dto.request.RootUserSearchRequest;
import tech.cspioneer.backend.entity.dto.request.UserCreateRequest;
import tech.cspioneer.backend.entity.dto.request.UserUpdateRequest;
import tech.cspioneer.backend.entity.dto.response.UserResponse;
import tech.cspioneer.backend.model.response.PagedResponse;

public interface RootUserManagementService {

    PagedResponse<UserResponse> getAllUsers(int page, int size, RootUserSearchRequest request);

    UserResponse getUserById(Long id);

    UserResponse getUserByUuid(String uuid);

    UserResponse createUser(UserCreateRequest userCreateRequest);

    UserResponse updateUser(String uuid, UserUpdateRequest userUpdateRequest);

    void deleteUser(String uuid);
}
