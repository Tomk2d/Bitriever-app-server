package com.bitreiver.app_server.domain.user.controller;

import com.bitreiver.app_server.domain.user.dto.AuthResponse;
import com.bitreiver.app_server.domain.user.dto.LogoutRequest;
import com.bitreiver.app_server.domain.user.dto.OAuth2CodePayload;
import com.bitreiver.app_server.domain.user.dto.OAuth2TokenRequest;
import com.bitreiver.app_server.domain.user.dto.SetNicknameRequest;
import com.bitreiver.app_server.domain.user.dto.SetProfileUrlRequest;
import com.bitreiver.app_server.domain.user.dto.UserLoginRequest;
import com.bitreiver.app_server.domain.user.dto.UserResponse;
import com.bitreiver.app_server.domain.user.dto.UserSignUpRequest;
import com.bitreiver.app_server.domain.user.service.OAuth2CodeService;
import com.bitreiver.app_server.domain.user.service.UserService;
import com.bitreiver.app_server.global.common.exception.CustomException;
import com.bitreiver.app_server.global.common.exception.ErrorCode;
import com.bitreiver.app_server.global.common.response.ApiResponse;
import com.bitreiver.app_server.global.security.AuthCookieHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "사용자 인증 및 관리 API")
public class UserController {
    
    private final UserService userService;
    private final AuthCookieHelper authCookieHelper;
    private final OAuth2CodeService oAuth2CodeService;
    
    @Operation(summary = "OAuth2 code 교환", description = "OAuth2 성공 시 발급된 code로 access token과 refresh token(쿠키)을 발급받습니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 발급 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "code가 유효하지 않거나 만료됨")
    })
    @PostMapping("/oauth/token")
    public ApiResponse<AuthResponse> exchangeOAuth2Code(@Valid @RequestBody OAuth2TokenRequest request,
                                                        HttpServletRequest httpRequest,
                                                        HttpServletResponse httpResponse) {
        OAuth2CodePayload payload = oAuth2CodeService.getAndDelete(request.getCode())
            .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));
        httpResponse.addHeader(HttpHeaders.SET_COOKIE,
            authCookieHelper.createRefreshTokenCookie(payload.getRefreshToken(), httpRequest).toString());
        AuthResponse authResponse = AuthResponse.builder()
            .userId(payload.getUserId())
            .email(payload.getEmail())
            .nickname(payload.getNickname())
            .profileUrl(payload.getProfileUrl())
            .accessToken(payload.getAccessToken())
            .requiresNickname(payload.getRequiresNickname() != null ? payload.getRequiresNickname() : false)
            .build();
        return ApiResponse.success(authResponse, "토큰이 발급되었습니다.");
    }
    
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원가입 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 이메일/닉네임")
    })
    @PostMapping("/signup")
    public ApiResponse<Void> signUp(@Valid @RequestBody UserSignUpRequest request) {
        userService.signUp(request);
        return ApiResponse.success(null, "회원가입이 완료되었습니다.");
    }
    
    @Operation(summary = "로그인", description = "사용자 로그인 및 JWT 토큰 발급")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody UserLoginRequest request,
                                          HttpServletRequest httpRequest,
                                          HttpServletResponse httpResponse) {
        var result = userService.login(request);
        httpResponse.addHeader(HttpHeaders.SET_COOKIE,
            authCookieHelper.createRefreshTokenCookie(result.getRefreshToken(), httpRequest).toString());
        return ApiResponse.success(result.getAuthResponse(), "로그인되었습니다.");
    }
    
    @Operation(summary = "닉네임 중복 확인", description = "닉네임의 사용 가능 여부를 확인합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "확인 성공")
    })
    @GetMapping("/check-nickname")
    public ApiResponse<Boolean> checkNickname(@RequestParam(value = "nickname") String nickname) {
        boolean isAvailable = userService.checkNicknameAvailable(nickname);
        return ApiResponse.success(isAvailable);
    }
    
    @Operation(summary = "토큰 갱신", description = "Refresh token을 사용하여 새로운 access token과 refresh token을 발급받습니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Refresh token이 유효하지 않거나 만료됨")
    })
    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refreshToken(HttpServletRequest httpRequest,
                                                  HttpServletResponse httpResponse) {
        String refreshToken = extractRefreshTokenFromCookie(httpRequest);
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        var result = userService.refreshToken(refreshToken);
        httpResponse.addHeader(HttpHeaders.SET_COOKIE,
            authCookieHelper.createRefreshTokenCookie(result.getRefreshToken(), httpRequest).toString());
        return ApiResponse.success(result.getAuthResponse(), "토큰이 갱신되었습니다.");
    }
    
    @Operation(summary = "로그아웃", description = "사용자 로그아웃을 처리하고 토큰을 무효화합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "JWT")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(Authentication authentication, HttpServletRequest httpRequest,
                                   HttpServletResponse httpResponse, @RequestBody(required = false) LogoutRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        String accessToken = null;
        if (request != null && request.getAccessToken() != null) {
            accessToken = request.getAccessToken();
        } else {
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                accessToken = authHeader.substring(7);
            }
        }
        String refreshToken = extractRefreshTokenFromCookie(httpRequest);
        userService.logout(userId, accessToken, refreshToken);
        httpResponse.addHeader(HttpHeaders.SET_COOKIE,
            authCookieHelper.clearRefreshTokenCookie(httpRequest).toString());
        return ApiResponse.success(null, "로그아웃되었습니다.");
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        Optional<String> value = Arrays.stream(request.getCookies())
            .filter(c -> AuthCookieHelper.REFRESH_TOKEN_COOKIE_NAME.equals(c.getName()))
            .map(jakarta.servlet.http.Cookie::getValue)
            .findFirst();
        return value.orElse(null);
    }
    
    @Operation(summary = "현재 사용자 정보 조회", description = "JWT 토큰을 통해 현재 로그인한 사용자의 정보를 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "JWT")
    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrentUser(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        UserResponse response = userService.getCurrentUser(userId);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "닉네임 설정", description = "SNS 회원가입 시 닉네임을 설정합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "닉네임 설정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "이미 닉네임이 설정되어 있음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 사용 중인 닉네임"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "JWT")
    @PostMapping("/set-nickname")
    public ApiResponse<Void> setNickname(
            Authentication authentication,
            @Valid @RequestBody SetNicknameRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        userService.setNickname(userId, request.getNickname());
        return ApiResponse.success(null, "닉네임이 설정되었습니다.");
    }

    @Operation(summary = "프로필 이미지 변경", description = "사용자 프로필 이미지를 변경합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 프로필 값"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "JWT")
    @PostMapping("/set-profile-url")
    public ApiResponse<Void> setProfileUrl(
            Authentication authentication,
            @Valid @RequestBody SetProfileUrlRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        userService.setProfileUrl(userId, request.getProfileUrl());
        return ApiResponse.success(null, "프로필이 변경되었습니다.");
    }
}

