package com.bitreiver.app_server.domain.user.service;

import com.bitreiver.app_server.domain.user.dto.AuthResponse;
import com.bitreiver.app_server.domain.user.dto.UserLoginRequest;
import com.bitreiver.app_server.domain.user.dto.UserResponse;
import com.bitreiver.app_server.domain.user.dto.UserSignUpRequest;

import java.util.UUID;

public interface UserService {
    void signUp(UserSignUpRequest request);
    AuthResponse login(UserLoginRequest request);
    void logout(UUID userId);
    boolean checkNicknameAvailable(String nickname);
    UserResponse getCurrentUser(UUID userId);
}

