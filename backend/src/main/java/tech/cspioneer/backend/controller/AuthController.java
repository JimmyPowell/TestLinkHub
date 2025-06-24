package tech.cspioneer.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.entity.dto.request.*;
import tech.cspioneer.backend.entity.dto.response.LoginResponse;
import tech.cspioneer.backend.exception.VerificationCodeException;
import tech.cspioneer.backend.model.response.ApiResponse;
import tech.cspioneer.backend.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    //注册相关
    /**
     * 普通个人用户，支持使用邮箱或手机号注册；
     * 企业用户，支持使用企业邮箱注册；
     * root用户默认无法注册，系统自带
     *
     * 先实现个人和企业用户使用邮箱进行注册的方式，
     * 然后再实现手机号注册。
     *
     * */

    /**
     * 邮箱注册的流程：
     * 首先，输入邮箱地址，然后请求验证码端点；
     * 后端生成验证码，并把邮箱--{验证码、激活状态}写入redis；
     * 后端调用邮件工具类，向该邮箱发送验证信息；
     *
     * 用户收到验证码后，调用激活端点，验证验证码；
     * 如果正确，则把redis里的激活状态设置为true；
     * 然后，用户可以进行详细用户信息的填写，
     * 最后请求注册端点，将信息写入。
     *
     *
     * */


    //接受邮箱地址，发送激活邮件

    @PostMapping("/generate-verify-code")
    public ResponseEntity<ApiResponse<Void>> generateVerifyCode(@RequestBody EmailRequest emailRequest) {
        String email = emailRequest.getEmail();
        //校验邮箱不为空
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Email is required"));
        }

        // 使用正则表达式校验邮箱格式
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        if (!email.matches(emailRegex)) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Invalid email format"));
        }
        //>TODO 检查重复
        try {
            authService.generateAndSendVerificationCode(email);
            return ResponseEntity.ok(ApiResponse.success(200, "Verification code sent successfully. Please check your email.", null));
        } catch (VerificationCodeException e) {
            // 捕获自定义的业务异常
            return ResponseEntity.badRequest().body(ApiResponse.error(4001, e.getMessage()));
        } catch (Exception e) {
            // 捕获其他意外的系统异常
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, "An unexpected error occurred."));
        }
    }



    //验证验证码，激活用户

    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponse<Void>> verifyCode(@RequestBody VerifyCoderequest verifyCoderequest) {

        String code = verifyCoderequest.getCode();
        String email = verifyCoderequest.getEmail();

        if (code == null || code.trim().isEmpty()|| email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Code or email is required"));
        }

        try {
            authService.verifyCode(code, email);
            return ResponseEntity.ok(ApiResponse.success(200, "Email verification successful!", null));
        } catch (VerificationCodeException e) {
            // 捕获自定义的业务异常
            return ResponseEntity.badRequest().body(ApiResponse.error(4001, e.getMessage()));
        } catch (Exception e) {
            // 捕获其他意外的系统异常
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, "An unexpected error occurred."));
        }

    }


    //注册，提交用户账号信息

    //普通用户注册
    @PostMapping("/register/individual")
    public ResponseEntity<ApiResponse<Void>> registerIndividual(@RequestBody IndividualRegisterRequest individualRegisterRequest) {
        String email = individualRegisterRequest.getEmail();

        //校验邮箱不为空
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Email is required"));
        }

        // 使用正则表达式校验邮箱格式
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        if (!email.matches(emailRegex)) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Invalid email format"));
        }

        try {

            authService.registerIndividual(individualRegisterRequest);
            return ResponseEntity.ok(ApiResponse.success(200, "Registration successful!", null));
        } catch (VerificationCodeException e) {
            // 捕获自定义的业务异常
            return ResponseEntity.badRequest().body(ApiResponse.error(4001, e.getMessage()));
        } catch (Exception e) {
            // 捕获其他意外的系统异常
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, "An unexpected error occurred."));
        }



    }




    //企业用户注册
    @PostMapping("/register/enterprise")
    public ResponseEntity<ApiResponse<Void>> registerEnterprise(@RequestBody EnterpriseRegisterRequest enterpriseRegisterRequest) {
        try {
            authService.registerEnterprise(enterpriseRegisterRequest);
            return ResponseEntity.ok(ApiResponse.success(200, "公司及管理员账户注册成功！", null));
        } catch (VerificationCodeException e) {
            // 捕获自定义的业务异常
            return ResponseEntity.ok(ApiResponse.error(4001, e.getMessage()));
        } catch (Exception e) {
            // 捕获其他意外的系统异常
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, e.getMessage()));
        }
    } //>TODO:修复状态穿透的问题



    //登录，分为个人用户登陆或超级管理员登陆、公司登陆两个端点
    @PostMapping("/login/user")
    public ResponseEntity<ApiResponse<LoginResponse>> userLogin(@RequestBody UserLoginRequest loginRequest) {
        try {
            LoginResponse loginResponse = authService.userLogin(loginRequest);
            return ResponseEntity.ok(ApiResponse.success(200, "登录成功！", loginResponse));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, e.getMessage()));
        } catch (Exception e) {
            // 捕获其他意外的系统异常
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, "登录时发生意外错误。"));
        }
    }
    
    @PostMapping("/login/company")
    public ResponseEntity<ApiResponse<LoginResponse>> companyLogin(@RequestBody CompanyLoginrequest loginRequest) {
        try {
            LoginResponse loginResponse = authService.companyLogin(loginRequest);
            return ResponseEntity.ok(ApiResponse.success(200, "登录成功！", loginResponse));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, e.getMessage()));
        } catch (Exception e) {
            // 捕获其他意外的系统异常
            return ResponseEntity.internalServerError().body(ApiResponse.error(5000, e.getMessage()));
        }
    }

    //退出

    //忘记密码

    //刷新令牌




    //修改密码

}
