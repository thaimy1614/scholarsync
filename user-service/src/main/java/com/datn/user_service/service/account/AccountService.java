package com.datn.user_service.service.account;

import com.datn.user_service.dto.request.*;
import com.datn.user_service.dto.response.*;
import com.datn.user_service.model.Account;
import com.nimbusds.jose.JOSEException;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AccountService {
    LoginResponse authenticate(LoginRequest loginRequest) throws JOSEException, MalformedURLException;

    IntrospectResponse introspect(String token);

    RegisterResponse registerOne(RegisterUser signupRequest);

    RegisterResponse registerPrincipal(RegisterUser signupRequest);

    RegisterResponse registerBatch(MultipartFile file, String type);

    void logout(String token) throws Exception;

    LoginResponse outboundAuthenticate(String code) throws JOSEException, MalformedURLException, UnsupportedEncodingException;

    RefreshTokenResponse refreshToken(RefreshTokenRequest request) throws ParseException, JOSEException, MalformedURLException;

    String verifyAccount(String email, String token);

    SendOTPResponse sendOTPForForgetPassword(String email);

    CheckOTPResponse checkOTP(String otp, String email);

    Map<String, Boolean> checkUserRoles(List<String> userIds, String roleName);

    ChangePasswordResponse changePassword(String userId, ChangePasswordRequest request);

    List<Object> findByEmails(List<String> emails);


}
