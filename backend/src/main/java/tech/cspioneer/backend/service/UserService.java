package tech.cspioneer.backend.service;

import tech.cspioneer.backend.entity.dto.response.CurrentUserResponse;

public interface UserService {
    CurrentUserResponse getCurrentUserInfo(String uuid, String role);
}
