package tech.cspioneer.backend.service;

import tech.cspioneer.backend.entity.dto.request.UserCreateRequest;
import tech.cspioneer.backend.entity.dto.request.UserUpdateRequest;
import tech.cspioneer.backend.entity.dto.response.UserResponse;
import tech.cspioneer.backend.model.response.PagedResponse;

public interface CompanyUserManagementService {

    PagedResponse<UserResponse> getCompanyUsers(int page, int size, String currentUserUuid);

    UserResponse createCompanyUser(UserCreateRequest userCreateRequest, String currentUserUuid);

    UserResponse updateCompanyUser(String uuid, UserUpdateRequest userUpdateRequest, String currentUserUuid);

    void deleteCompanyUser(String uuid, String currentUserUuid);

    void removeUserFromCompany(String uuid, String currentUserUuid);
}
