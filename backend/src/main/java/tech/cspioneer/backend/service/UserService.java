package tech.cspioneer.backend.service;

import tech.cspioneer.backend.entity.dto.request.UpdateUserRequest;
import tech.cspioneer.backend.entity.dto.response.CurrentUserResponse;
import tech.cspioneer.backend.entity.dto.response.UserInfoResponse;

public interface UserService {
    CurrentUserResponse getCurrentUserInfo(String uuid, String role);

    UserInfoResponse getUserInfo(String uuid);

    UserInfoResponse updateUserInfo(String uuid, UpdateUserRequest updateUserRequest);
}
