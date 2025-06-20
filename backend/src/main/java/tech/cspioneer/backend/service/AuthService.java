package tech.cspioneer.backend.service;

import tech.cspioneer.backend.entity.dto.request.EnterpriseRegisterRequest;
import tech.cspioneer.backend.entity.dto.request.IndividualRegisterRequest;
import tech.cspioneer.backend.entity.dto.request.UserLoginRequest;
import tech.cspioneer.backend.entity.dto.request.CompanyLoginrequest;
import tech.cspioneer.backend.entity.dto.response.LoginResponse;
import tech.cspioneer.backend.exception.VerificationCodeException;

public interface AuthService {

    /**
     * Generates a verification code, stores it in Redis, and sends it to the user's email.
     * @param email The recipient's email address.
     * @throws VerificationCodeException if sending or storing the code fails.
     */
    void generateAndSendVerificationCode(String email) throws VerificationCodeException;

    /**
     * Verifies the code against the stored code for the given email.
     * @param code The verification code to verify.
     * @param email The email address associated with the code.
     * @throws VerificationCodeException if verification fails.
     */
    void verifyCode(String code, String email) throws VerificationCodeException;

    /**
     * Registers an individual user after validating the request.
     * @param request The registration request containing user details.
     * @throws VerificationCodeException if the email is not verified or other validation fails.
     */
    void registerIndividual(IndividualRegisterRequest request) throws VerificationCodeException;
    
    /**
     * Registers an enterprise user after validating the request.
     * Creates both a company and its administrator account.
     * @param request The registration request containing enterprise and admin user details.
     * @throws VerificationCodeException if the email is not verified or other validation fails.
     */
    void registerEnterprise(EnterpriseRegisterRequest request) throws VerificationCodeException;

    LoginResponse userLogin(UserLoginRequest loginRequest);

    LoginResponse companyLogin(CompanyLoginrequest loginRequest);
}
