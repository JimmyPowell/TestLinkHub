package tech.cspioneer.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import tech.cspioneer.backend.entity.Company;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.entity.dto.request.CompanyLoginrequest;
import tech.cspioneer.backend.entity.dto.request.IndividualRegisterRequest;
import tech.cspioneer.backend.entity.dto.request.EnterpriseRegisterRequest;
import tech.cspioneer.backend.entity.dto.request.UserLoginRequest;
import tech.cspioneer.backend.entity.dto.response.LoginResponse;
import tech.cspioneer.backend.entity.enums.CompanyStatus;
import tech.cspioneer.backend.entity.enums.UserRole;
import tech.cspioneer.backend.entity.enums.UserStatus;
import tech.cspioneer.backend.exception.VerificationCodeException;
import tech.cspioneer.backend.mapper.CompanyMapper;
import tech.cspioneer.backend.mapper.UserMapper;
import tech.cspioneer.backend.service.AuthService;
import tech.cspioneer.backend.utils.CodeGeneratorUtil;
import tech.cspioneer.backend.utils.JwtUtils;
import tech.cspioneer.backend.utils.RedisUtils;
import tech.cspioneer.backend.utils.SMTPUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final SMTPUtils smtpUtils;
    private final UserMapper userMapper;
    private final CompanyMapper companyMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    private static final String VERIFICATION_CODE_KEY_PREFIX = "verify:code:";
    private static final String VERIFIED_USER_KEY_PREFIX = "verified:user:";
    private static final long VERIFICATION_CODE_EXPIRATION_MINUTES = 5;

    @Override
    public void generateAndSendVerificationCode(String email) throws VerificationCodeException {
        String code = CodeGeneratorUtil.generateSixDigitCode();
        String redisKey = VERIFICATION_CODE_KEY_PREFIX + email;
        log.info("Generating verification code for email: {}, code: {}, redisKey: {}", email, code, redisKey);

        try {
            // 使用 RedisUtils 存储验证码，并设置过期时间（单位：秒）
            int expirationInSeconds = (int) (VERIFICATION_CODE_EXPIRATION_MINUTES * 60);
            log.info("Storing key '{}' in Redis DB 0 with expiration {} seconds.", redisKey, expirationInSeconds);
            RedisUtils.set(redisKey, code, expirationInSeconds, 0); // 使用 db 0
            log.info("Successfully stored verification code in Redis for key: {}", redisKey);
        } catch (Exception e) {
            // Since RedisUtils can throw Jedis-related exceptions, we catch a broad exception.
            log.error("Failed to store verification code in Redis for key: {}", redisKey, e);
            throw new VerificationCodeException("Could not store verification code.", e);
        }

        // 使用 SMTPUtils 发送邮件
        String subject = "Your Verification Code for TestLinkHub";
        
        // 创建一个美观的 HTML 邮件模板
        String content = createBeautifulEmailTemplate(code);

        boolean sent = smtpUtils.sendHtmlMail(email, subject, content);
        if (!sent) {
            // 如果发送失败，抛出业务异常
            throw new VerificationCodeException("Failed to send verification email.");
        }
    }
    
    /**
     * 创建美观的验证码邮件模板
     * 
     * @param code 验证码
     * @return HTML 格式的邮件内容
     */
    private String createBeautifulEmailTemplate(String code) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Email Verification</title>\n" +
                "    <style>\n" +
                "        @import url('https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500;700&display=swap');\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body style=\"margin: 0; padding: 0; font-family: 'Roboto', Arial, sans-serif; background-color: #f4f4f4; color: #333333;\">\n" +
                "    <div style=\"max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 6px 18px rgba(0, 0, 0, 0.1);\">\n" +
                "        <!-- Header with Gradient -->\n" +
                "        <div style=\"background: linear-gradient(135deg, #4285f4, #34a853); padding: 40px 0; text-align: center;\">\n" +
                "            <div style=\"display: inline-block; background-color: rgba(255, 255, 255, 0.2); border-radius: 50%; width: 80px; height: 80px; line-height: 80px; margin-bottom: 15px;\">\n" +
                "                <span style=\"font-size: 36px; color: #ffffff; font-weight: bold;\">TLH</span>\n" +
                "            </div>\n" +
                "            <h1 style=\"color: #ffffff; margin: 0; font-size: 28px; font-weight: 500;\">TestLinkHub</h1>\n" +
                "            <p style=\"color: rgba(255, 255, 255, 0.9); margin: 5px 0 0; font-size: 16px;\">Account Verification</p>\n" +
                "        </div>\n" +
                "        \n" +
                "        <!-- Main Content -->\n" +
                "        <div style=\"padding: 40px 30px;\">\n" +
                "            <!-- Greeting -->\n" +
                "            <h2 style=\"color: #333333; margin-top: 0; font-weight: 500; font-size: 24px;\">Verify Your Email Address</h2>\n" +
                "            <p style=\"color: #666666; line-height: 24px; font-size: 16px;\">Thank you for registering with TestLinkHub. To ensure the security of your account, please verify your email address using the code below:</p>\n" +
                "            \n" +
                "            <!-- Code Box -->\n" +
                "            <div style=\"background: linear-gradient(to right, #f8f8f8, #ffffff, #f8f8f8); border: 1px dashed #e0e0e0; border-radius: 8px; padding: 25px 15px; margin: 30px 0; text-align: center; position: relative;\">\n" +
                "                <div style=\"position: absolute; top: -12px; left: 50%; transform: translateX(-50%); background-color: #ffffff; padding: 0 15px;\">\n" +
                "                    <span style=\"color: #4285f4; font-size: 14px; font-weight: 500;\">VERIFICATION CODE</span>\n" +
                "                </div>\n" +
                "                <span style=\"font-size: 38px; font-weight: 700; color: #4285f4; letter-spacing: 8px; text-shadow: 1px 1px 2px rgba(0,0,0,0.1);\">" + code + "</span>\n" +
                "                <div style=\"position: absolute; bottom: -12px; left: 50%; transform: translateX(-50%); background-color: #ffffff; padding: 0 15px;\">\n" +
                "                    <span style=\"color: #999; font-size: 13px;\">Expires in " + VERIFICATION_CODE_EXPIRATION_MINUTES + " minutes</span>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "            \n" +
                "            <!-- Instructions -->\n" +
                "            <p style=\"color: #666666; line-height: 24px; font-size: 16px;\">Please enter this code on the verification page to complete your registration. If you did not request this code, please ignore this email or contact support.</p>\n" +
                "            \n" +
                "            <!-- Divider -->\n" +
                "            <div style=\"margin: 30px 0; height: 1px; background: linear-gradient(to right, transparent, #e0e0e0, transparent);\"></div>\n" +
                "            \n" +
                "            <!-- Security Note -->\n" +
                "            <div style=\"background-color: #fafafa; border-left: 4px solid #4285f4; padding: 15px; margin: 20px 0; border-radius: 4px;\">\n" +
                "                <p style=\"margin: 0; color: #666666; font-size: 14px;\">\n" +
                "                    <strong style=\"color: #4285f4;\">Security Tip:</strong> Never share your verification code with anyone, including TestLinkHub staff.\n" +
                "                </p>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "        \n" +
                "        <!-- Footer -->\n" +
                "        <div style=\"padding: 25px 30px; background-color: #fafafa; border-top: 1px solid #eeeeee; text-align: center;\">\n" +
                "            <p style=\"margin: 0; color: #999999; font-size: 14px;\">This is an automated message, please do not reply.</p>\n" +
                "            <div style=\"margin: 15px 0;\">\n" +
                "                <!-- Social Media Icons (Simplified) -->\n" +
                "                <span style=\"display: inline-block; width: 32px; height: 32px; line-height: 32px; background-color: #4285f4; border-radius: 50%; color: white; font-size: 14px; margin: 0 5px;\">f</span>\n" +
                "                <span style=\"display: inline-block; width: 32px; height: 32px; line-height: 32px; background-color: #1DA1F2; border-radius: 50%; color: white; font-size: 14px; margin: 0 5px;\">t</span>\n" +
                "                <span style=\"display: inline-block; width: 32px; height: 32px; line-height: 32px; background-color: #0077B5; border-radius: 50%; color: white; font-size: 14px; margin: 0 5px;\">in</span>\n" +
                "            </div>\n" +
                "            <p style=\"margin: 15px 0 0 0; color: #999999; font-size: 13px;\">&copy; " + java.time.Year.now().getValue() + " TestLinkHub. All rights reserved.</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    @Override
    public void verifyCode(String code, String email) throws VerificationCodeException {
        if (code == null || code.trim().isEmpty()) {
            throw new VerificationCodeException("Verification code cannot be empty.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new VerificationCodeException("Email cannot be empty.");
        }
        
        String redisKey = VERIFICATION_CODE_KEY_PREFIX + email;
        log.info("Verifying code for email: {}, redisKey: {}", email, redisKey);
        
        try {
            // 从 Redis 获取存储的验证码
            String storedCode = RedisUtils.get(redisKey, 0);
            log.info("Retrieved stored code from Redis for key: {}, code exists: {}", redisKey, storedCode != null);
            
            if (storedCode == null) {
                throw new VerificationCodeException("Verification code has expired or does not exist.");
            }
            
            // 验证码比对
            if (!code.equals(storedCode)) {
                log.warn("Code verification failed for email: {}, codes do not match", email);
                throw new VerificationCodeException("Invalid verification code.");
            }
            
            // 验证成功后，删除 Redis 中的验证码，防止重复使用
            RedisUtils.del(redisKey, 0);
            log.info("Code verification successful for email: {}, verification code removed from Redis", email);
            
            // 这里可以添加用户激活逻辑，例如更新用户状态为已验证
            // 如果需要，可以在 Redis 中存储用户的验证状态
            String verifiedKey = VERIFIED_USER_KEY_PREFIX + email;
            RedisUtils.set(verifiedKey, "true", 86400 * 30, 0); // 存储验证状态，有效期30天
            log.info("User verification status stored in Redis with key: {}", verifiedKey);
            
        } catch (VerificationCodeException e) {
            // 重新抛出验证码异常
            throw e;
        } catch (Exception e) {
            // 捕获其他可能的异常
            log.error("Error during code verification for email: {}", email, e);
            throw new VerificationCodeException("Failed to verify code due to system error.", e);
        }
    }

    @Override
    public void registerIndividual(IndividualRegisterRequest request) throws VerificationCodeException {
        // 1. 验证请求参数
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new VerificationCodeException("Email is required.");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new VerificationCodeException("Password is required.");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new VerificationCodeException("Name is required.");
        }
        
        String email = request.getEmail();
        
        // 2. 检查邮箱是否已经验证
        String verifiedKey = VERIFIED_USER_KEY_PREFIX + email;
        String isVerified = RedisUtils.get(verifiedKey, 0);
        if (isVerified == null || !isVerified.equals("true")) {
            log.warn("Email not verified for registration: {}", email);
            throw new VerificationCodeException("Email not verified. Please verify your email before registration.");
        }
        
        // 3. 检查邮箱是否已被注册
        User existingUser = userMapper.findByEmail(email);
        if (existingUser != null) {
            log.warn("Email already registered: {}", email);
            throw new VerificationCodeException("Email already registered. Please use a different email or recover your account.");
        }
        
        // 4. 创建新用户
        User newUser = User.builder()
                .uuid(UUID.randomUUID().toString())
                .name(request.getName())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword())) // 密码加密
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .avatarUrl(request.getAvatarUrl())
                .gender(request.getGender())
                .companyId(request.getCompanyId())
                .role(UserRole.USER) // 默认为普通用户角色
                .status(UserStatus.ACTIVE) // 默认为活跃状态
                .description(request.getDescription())
                .postCount(0) // 初始化计数器
                .lessonCount(0)
                .meetingCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // 5. 保存用户到数据库
        try {
            int result = userMapper.insert(newUser);
            if (result <= 0) {
                throw new VerificationCodeException("Failed to register user. Please try again.");
            }
            log.info("Successfully registered new individual user: {}, id: {}", email, newUser.getId());
            
            // 6. 注册成功后，可以删除Redis中的验证状态，因为不再需要了
            // 或者保留它，作为"最近验证过的邮箱"的标记
            // RedisUtils.del(verifiedKey, 0);
            
        } catch (Exception e) {
            log.error("Error during user registration: {}", email, e);
            throw new VerificationCodeException("Registration failed due to system error.", e);
        }
    }

    @Override
    public void registerEnterprise(EnterpriseRegisterRequest request) throws VerificationCodeException {
        log.info("Attempting to register enterprise with request: {}", request);

        // 1. 验证请求参数
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            log.warn("Validation failed: Company contact email is required.");
            throw new VerificationCodeException("公司联系邮箱是必填项。");
        }
        // 移除了对密码的强制校验，因为不再创建用户
        // if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
        //     log.warn("Validation failed: Admin password is required.");
        //     throw new VerificationCodeException("管理员密码是必填项。");
        // }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            log.warn("Validation failed: Company name is required.");
            throw new VerificationCodeException("公司名称是必填项。");
        }
        if (request.getCompanyCode() == null || request.getCompanyCode().trim().isEmpty()) {
            log.warn("Validation failed: Company code is required.");
            throw new VerificationCodeException("公司代码是必填项。");
        }
        log.info("Request parameters validated successfully for company email: {}", request.getEmail());

        String companyEmail = request.getEmail();

        // 2. 检查邮箱是否已经验证 (此处的邮箱是用于验证操作发起者的，不一定是公司最终的联系邮箱，但当前逻辑是复用的)
        String verifiedKey = VERIFIED_USER_KEY_PREFIX + companyEmail;
        String isVerified = RedisUtils.get(verifiedKey, 0);
        if (isVerified == null || !isVerified.equals("true")) {
            log.warn("Email used for verification is not verified, cannot register company: {}", companyEmail);
            throw new VerificationCodeException("请先验证您的邮箱，然后再进行注册。");
        }
        log.info("Email {} used for verification is verified.", companyEmail);

        // 3. 检查该邮箱是否已被注册为用户 (根据新需求)
        User existingUser = userMapper.findByEmail(companyEmail);
        if (existingUser != null) {
            log.warn("Email {} is already registered as a user. Cannot proceed with company registration under this email as primary contact if it's a user.", companyEmail);
            throw new VerificationCodeException("该邮箱已被注册为个人用户，请使用其他邮箱进行公司注册，或联系支持人员。");
        }
        log.info("Email {} is not registered as a user, proceeding with company registration.", companyEmail);

        // 4. 检查公司代码是否已被使用
        Company existingCompanyWithCode = companyMapper.findByCompanyCode(request.getCompanyCode());
        if (existingCompanyWithCode != null) {
            log.warn("Company code {} is already in use.", request.getCompanyCode());
            throw new VerificationCodeException("公司代码已被占用，请输入一个唯一的公司代码。");
        }
        log.info("Company code {} is available.", request.getCompanyCode());

        // 5. 检查公司邮箱是否已被其他公司使用 (可选，但推荐)
        Company existingCompanyWithEmail = companyMapper.findByEmail(companyEmail);
        if (existingCompanyWithEmail != null) {
            log.warn("Email {} is already in use by another company.", companyEmail);
            throw new VerificationCodeException("该邮箱已被其他公司注册，请使用其他邮箱。");
        }
        log.info("Email {} is available for company registration.", companyEmail);


        // 6. 创建并保存公司实体
        Company newCompany = Company.builder()
                .uuid(UUID.randomUUID().toString())
                .name(request.getName())
                .email(companyEmail) // 使用请求中的 email 作为公司的联系邮箱
                .password(passwordEncoder.encode(request.getPassword())) // 加密并设置公司密码
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .avatarUrl(request.getAvatarUrl())
                .companyCode(request.getCompanyCode())
                .status(CompanyStatus.ACTIVE) // 公司默认为激活状态
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        log.info("Attempting to insert new company: {}", newCompany);

        try {
            companyMapper.insert(newCompany);
            log.info("Successfully created new company: {}, ID: {}. Email: {}", newCompany.getName(), newCompany.getId(), newCompany.getEmail());

            // 7. 注册成功后，可以删除Redis中的验证状态
            RedisUtils.del(verifiedKey, 0);
            log.info("Removed verification status from Redis for email: {}", companyEmail);

        } catch (Exception e) {
            log.error("Failed to create company '{}'. Request: {}. Exception: ", newCompany.getName(), request, e);
            throw new VerificationCodeException("公司注册失败，请稍后重试。", e);
        }
        log.info("Enterprise registration completed successfully for company: {}. Email: {}", newCompany.getName(), newCompany.getEmail());
    }

    @Override
    public LoginResponse userLogin(UserLoginRequest loginRequest) {
        log.info("Attempting login for user with email: {}", loginRequest.getEmail());

        // 1. 根据邮箱查找用户
        User user = userMapper.findByEmail(loginRequest.getEmail());
        if (user == null) {
            log.warn("Login failed: User not found with email: {}", loginRequest.getEmail());
            throw new BadCredentialsException("用户名或密码错误");
        }

        // 2. 校验密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Login failed: Invalid password for user with email: {}", loginRequest.getEmail());
            throw new BadCredentialsException("用户名或密码错误");
        }

        // 3. 检查用户状态
        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("Login failed: User account is not active. Status: {}", user.getStatus());
            throw new BadCredentialsException("用户账户已被锁定或未激活");
        }

        // 4. 生成Token
        String accessToken = jwtUtils.generateAccessToken(user.getUuid(), user.getRole().name());
        // Refresh token 版本可以存储在数据库中，用于强制下线，这里暂时用 1
        String refreshToken = jwtUtils.generateRefreshToken(user.getUuid(), user.getRole().name(), 1);

        log.info("Login successful for user: {}", user.getEmail());
        return new LoginResponse(accessToken, refreshToken);
    }

    @Override
    public LoginResponse companyLogin(CompanyLoginrequest loginRequest) {
        log.info("Attempting login for company with email: {}", loginRequest.getEmail());

        // 1. 根据邮箱查找公司
        Company company = companyMapper.findByEmail(loginRequest.getEmail());
        if (company == null) {
            log.warn("Login failed: Company not found with email: {}", loginRequest.getEmail());
            throw new BadCredentialsException("公司邮箱或密码错误");
        }

        // 2. 校验密码
        // 注意：公司注册时也需要对密码进行加密存储
        if (company.getPassword() == null || !passwordEncoder.matches(loginRequest.getPassword(), company.getPassword())) {
            log.warn("Login failed: Invalid password for company with email: {}", loginRequest.getEmail());
            throw new BadCredentialsException("公司邮箱或密码错误");
        }

        // 3. 检查公司状态
        if (company.getStatus() != CompanyStatus.ACTIVE) {
            log.warn("Login failed: Company account is not active. Status: {}", company.getStatus());
            throw new BadCredentialsException("公司账户已被锁定或未激活");
        }

        // 4. 生成Token
        String accessToken = jwtUtils.generateAccessToken(company.getUuid(), "COMPANY");
        String refreshToken = jwtUtils.generateRefreshToken(company.getUuid(), "COMPANY", 1);

        log.info("Login successful for company: {}", company.getEmail());
        return new LoginResponse(accessToken, refreshToken);
    }
}
