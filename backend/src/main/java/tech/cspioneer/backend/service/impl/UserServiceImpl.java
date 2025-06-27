package tech.cspioneer.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.cspioneer.backend.entity.Company;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.entity.dto.response.CurrentUserResponse;
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
}
