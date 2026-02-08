package com.bitreiver.app_server.domain.user.service;

import com.bitreiver.app_server.domain.user.dto.AuthResponse;
import com.bitreiver.app_server.domain.user.dto.AuthResult;
import com.bitreiver.app_server.domain.user.dto.UserLoginRequest;
import com.bitreiver.app_server.domain.user.dto.UserResponse;
import com.bitreiver.app_server.domain.user.dto.UserSignUpRequest;
import com.bitreiver.app_server.domain.user.entity.User;
import com.bitreiver.app_server.domain.user.enums.SnsProvider;
import com.bitreiver.app_server.domain.user.repository.UserRepository;
import com.bitreiver.app_server.domain.notification.service.NotificationService;
import com.bitreiver.app_server.domain.notification.enums.NotificationType;
import com.bitreiver.app_server.global.common.exception.CustomException;
import com.bitreiver.app_server.global.common.exception.ErrorCode;
import com.bitreiver.app_server.global.security.jwt.JwtTokenProvider;
import com.bitreiver.app_server.global.security.jwt.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final NotificationService notificationService;
    
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

        notificationService.createNotification(
            user.getId(),
            NotificationType.SYSTEM,
            "회원가입을 환영합니다.",
            user.getNickname() + "님, Bitriever 의 새로운 회원이 되신것을 환영합니다. 다양한 거래소를 연동하고 매매일지, 종목분석, 투자전략공유 등의 다양한 기능을 이용해보세요!",
            null
        );
    }
    
    @Override
    @Transactional
    public AuthResult login(UserLoginRequest request) {
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
        
        AuthResponse authResponse = AuthResponse.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .profileUrl(user.getProfileUrl())
            .accessToken(accessToken)
            .build();
        return AuthResult.builder().authResponse(authResponse).refreshToken(refreshToken).build();
    }
    
    @Override
    @Transactional
    public AuthResult refreshToken(String refreshToken) {
        // Refresh token 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        
        // Refresh token 만료 확인
        if (jwtTokenProvider.isTokenExpired(refreshToken)) {
            throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }
        
        // Refresh token 블랙리스트 확인
        if (tokenBlacklistService.isRefreshTokenBlacklisted(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        
        // Refresh token에서 사용자 ID 추출
        UUID userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        
        // 사용자 존재 여부 확인
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        if (!user.getIsActive()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        
        // 기존 refresh token을 블랙리스트에 추가 (토큰 재사용 방지)
        tokenBlacklistService.addRefreshTokenToBlacklist(refreshToken);
        
        // 새로운 토큰 발급
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        
        AuthResponse authResponse = AuthResponse.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .profileUrl(user.getProfileUrl())
            .accessToken(newAccessToken)
            .build();
        return AuthResult.builder().authResponse(authResponse).refreshToken(newRefreshToken).build();
    }
    
    @Override
    @Transactional
    public AuthResult processOAuth2User(String provider, String snsId, String email, String nickname) {
        SnsProvider snsProvider = SnsProvider.fromRegistrationId(provider);
        
        // 기존 사용자 조회 (snsProvider와 snsId로)
        Optional<User> existingUser = userRepository.findBySnsProviderAndSnsId(snsProvider.getCode(), snsId);
        
        User user;
        if (existingUser.isPresent()) {
            // 기존 사용자 업데이트
            user = existingUser.get();
            user.updateLastLogin();
            
            // 이메일이나 닉네임이 변경되었을 수 있으므로 업데이트
            if (email != null && !email.isEmpty() && !email.equals(user.getEmail())) {
                // 이메일 중복 확인
                if (userRepository.existsByEmail(email) && !user.getEmail().equals(email)) {
                    throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
                }
                // 이메일 업데이트는 제한적 (SNS 사용자는 이메일 변경 불가)
            }
            
            userRepository.save(user);
        } else {
            // 신규 사용자 생성 - 임시 닉네임 부여 (나중에 수정), 이메일이 없으면 임시 이메일 생성
            String userEmail = email;
            if (userEmail == null || userEmail.isEmpty()) {
                userEmail = provider.toLowerCase() + "_" + snsId + "@temp.com";
            } else {
                // 이메일 중복 확인
                if (userRepository.existsByEmail(userEmail)) {
                    throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
                }
            }
            
            String tempNickname = "user_" + UUID.randomUUID().toString().substring(0, 8);
            
            // 임시 닉네임 중복 확인
            while (userRepository.existsByNickname(tempNickname)) {
                tempNickname = "user_" + UUID.randomUUID().toString().substring(0, 8);
            }
            
            user = User.builder()
                .id(UUID.randomUUID())
                .email(userEmail)
                .nickname(tempNickname) 
                .signupType((short) 1) 
                .snsProvider(snsProvider.getCode())
                .snsId(snsId)
                .passwordHash(null) 
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .isConnectExchange(false)
                .build();
            
            userRepository.save(user);

            notificationService.createNotification(
                user.getId(),
                NotificationType.SYSTEM,
                "회원가입을 환영합니다.",
                "Bitriever 의 새로운 회원이 되신것을 환영합니다. 다양한 거래소를 연동하고 매매일지, 종목분석, 투자전략공유 등의 다양한 기능을 이용해보세요!",
                null
            );
            
            // 신규 사용자는 닉네임 설정이 필요함
            // JWT 토큰 발급
            String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
            
            AuthResponse authResponse = AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(tempNickname) // 임시 닉네임 반환
                .profileUrl(user.getProfileUrl())
                .accessToken(accessToken)
                .requiresNickname(true) // 닉네임 설정 필요
                .build();
            return AuthResult.builder().authResponse(authResponse).refreshToken(refreshToken).build();
        }
        
        // 기존 사용자는 정상적으로 토큰 발급
        // JWT 토큰 발급
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        
        AuthResponse authResponse = AuthResponse.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .profileUrl(user.getProfileUrl())
            .accessToken(accessToken)
            .requiresNickname(false) // 기존 사용자는 닉네임 설정 불필요
            .build();
        return AuthResult.builder().authResponse(authResponse).refreshToken(refreshToken).build();
    }
    
    @Override
    @Transactional
    public void logout(UUID userId, String accessToken, String refreshToken) {
        // 사용자 존재 여부 확인
        if (!userRepository.existsById(userId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 토큰을 블랙리스트에 추가하여 무효화
        tokenBlacklistService.addTokensToBlacklist(accessToken, refreshToken);
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
    
    @Override
    @Transactional
    public void setNickname(UUID userId, String nickname) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 동일 닉네임이면 변경 없음
        if (nickname.equals(user.getNickname())) {
            return;
        }

        // 닉네임 유효성 검사
        validateNickname(nickname);

        // 닉네임 중복 확인 (다른 사용자가 이미 사용 중인 경우만)
        if (userRepository.existsByNickname(nickname)) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        // 닉네임 설정/변경
        user.setNickname(nickname);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void setProfileUrl(UUID userId, String profileUrl) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.setProfileUrl(profileUrl);
        userRepository.save(user);
    }
}

