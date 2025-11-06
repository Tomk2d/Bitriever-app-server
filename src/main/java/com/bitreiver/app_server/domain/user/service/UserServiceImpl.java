package com.bitreiver.app_server.domain.user.service;

import com.bitreiver.app_server.domain.user.dto.AuthResponse;
import com.bitreiver.app_server.domain.user.dto.UserLoginRequest;
import com.bitreiver.app_server.domain.user.dto.UserResponse;
import com.bitreiver.app_server.domain.user.dto.UserSignUpRequest;
import com.bitreiver.app_server.domain.user.entity.User;
import com.bitreiver.app_server.domain.user.repository.UserRepository;
import com.bitreiver.app_server.global.common.exception.CustomException;
import com.bitreiver.app_server.global.common.exception.ErrorCode;
import com.bitreiver.app_server.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    @Override
    @Transactional
    public void signUp(UserSignUpRequest request) {
        validateNickname(request.getNickname());
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }
        
        User user = User.builder()
            .id(UUID.randomUUID())
            .email(request.getEmail())
            .nickname(request.getNickname())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .signupType((short) 0)
            .createdAt(LocalDateTime.now())
            .isActive(true)
            .isConnectExchange(false)
            .build();
        
        userRepository.save(user);
    }
    
    @Override
    @Transactional
    public AuthResponse login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new CustomException(ErrorCode.WRONG_PASSWORD);
        }
        
        if (!user.getIsActive()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        
        user.updateLastLogin();
        userRepository.save(user);
        
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        
        return AuthResponse.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }
    
    @Override
    @Transactional
    public void logout(UUID userId) {
        // 사용자 존재 여부 확인
        if (!userRepository.existsById(userId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 로그아웃 처리 (향후 토큰 블랙리스트 기능 추가 가능)
        // 현재는 클라이언트에서 토큰을 삭제하는 방식으로 처리
    }
    
    @Override
    public boolean checkNicknameAvailable(String nickname) {
        validateNickname(nickname);
        return !userRepository.existsByNickname(nickname);
    }
    
    private void validateNickname(String nickname) {
        if (nickname == null || nickname.length() > 20) {
            throw new CustomException(ErrorCode.INVALID_NICKNAME);
        }
        
        for (char c : nickname.toCharArray()) {
            if (isHangulJamo(c)) {
                throw new CustomException(ErrorCode.INVALID_NICKNAME);
            }
        }
    }
    
    private boolean isHangulJamo(char c) {
        return (c >= 0x3131 && c <= 0x314E) || (c >= 0x314F && c <= 0x3163);
    }
    
    @Override
    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        return UserResponse.from(user);
    }
}

