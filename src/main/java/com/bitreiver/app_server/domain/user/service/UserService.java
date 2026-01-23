package com.bitreiver.app_server.domain.user.service;

import com.bitreiver.app_server.domain.user.dto.AuthResponse;
import com.bitreiver.app_server.domain.user.dto.UserLoginRequest;
import com.bitreiver.app_server.domain.user.dto.UserResponse;
import com.bitreiver.app_server.domain.user.dto.UserSignUpRequest;

import java.util.UUID;

public interface UserService {
    void signUp(UserSignUpRequest request);
    AuthResponse login(UserLoginRequest request);
    AuthResponse refreshToken(String refreshToken);
    AuthResponse processOAuth2User(String provider, String snsId, String email, String nickname);
    void logout(UUID userId, String accessToken, String refreshToken);
    boolean checkNicknameAvailable(String nickname);
    UserResponse getCurrentUser(UUID userId);
    void setNickname(UUID userId, String nickname); // SNS 회원가입 시 닉네임 설정
}

