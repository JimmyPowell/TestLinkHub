package tech.cspioneer.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.cspioneer.backend.entity.Company;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.entity.dto.request.UpdateUserRequest;
import tech.cspioneer.backend.entity.dto.response.CurrentUserResponse;
import tech.cspioneer.backend.entity.dto.response.UserInfoResponse;
import tech.cspioneer.backend.entity.enums.Gender;
import tech.cspioneer.backend.exception.ResourceNotFoundException;
import tech.cspioneer.backend.mapper.CompanyMapper;
import tech.cspioneer.backend.mapper.UserMapper;
import tech.cspioneer.backend.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final CompanyMapper companyMapper;

    @Override
    public CurrentUserResponse getCurrentUserInfo(String uuid, String role) {
        String name;
        if ("COMPANY".equals(role)) {
            Company company = companyMapper.findByUuid(uuid);
            if (company == null) {
                throw new ResourceNotFoundException("Company not found with uuid: " + uuid);
            }
            name = company.getName();
        } else {
            User user = userMapper.findByUuid(uuid);
            if (user == null) {
                throw new ResourceNotFoundException("User not found with uuid: " + uuid);
            }
            name = user.getName();
        }
        return new CurrentUserResponse(name, role);
    }

    @Override
    public UserInfoResponse getUserInfo(String uuid) {
        User user = userMapper.findByUuid(uuid);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with uuid: " + uuid);
        }
        return convertToUserInfoResponse(user);
    }

    @Override
    public UserInfoResponse updateUserInfo(String uuid, UpdateUserRequest updateUserRequest) {
        User user = userMapper.findByUuid(uuid);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with uuid: " + uuid);
        }

        user.setName(updateUserRequest.getName());
        user.setPhoneNumber(updateUserRequest.getPhoneNumber());
        user.setAddress(updateUserRequest.getAddress());
        user.setAvatarUrl(updateUserRequest.getAvatarUrl());
        if (updateUserRequest.getGender() != null) {
            user.setGender(Gender.valueOf(updateUserRequest.getGender().toUpperCase()));
        }
        user.setDescription(updateUserRequest.getDescription());

        userMapper.update(user);

        return convertToUserInfoResponse(user);
    }

    private UserInfoResponse convertToUserInfoResponse(User user) {
        UserInfoResponse userInfoResponse = new UserInfoResponse();
        userInfoResponse.setUuid(user.getUuid());
        userInfoResponse.setName(user.getName());
        userInfoResponse.setEmail(user.getEmail());
        userInfoResponse.setPhoneNumber(user.getPhoneNumber());
        userInfoResponse.setAddress(user.getAddress());
        userInfoResponse.setAvatarUrl(user.getAvatarUrl());
        userInfoResponse.setGender(user.getGender());
        if (user.getCompanyId() != null) {
            Company company = companyMapper.findById(user.getCompanyId());
            if (company != null) {
                userInfoResponse.setCompanyName(company.getName());
            }
        }
        userInfoResponse.setStatus(user.getStatus());
        userInfoResponse.setDescription(user.getDescription());
        userInfoResponse.setCreatedAt(user.getCreatedAt());
        userInfoResponse.setUpdatedAt(user.getUpdatedAt());
        return userInfoResponse;
    }
}
