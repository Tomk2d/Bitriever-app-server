package com.bitreiver.app_server.domain.user.controller;

import com.bitreiver.app_server.domain.user.dto.AuthResponse;
import com.bitreiver.app_server.domain.user.dto.LogoutRequest;
import com.bitreiver.app_server.domain.user.dto.RefreshTokenRequest;
import com.bitreiver.app_server.domain.user.dto.SetNicknameRequest;
import com.bitreiver.app_server.domain.user.dto.UserLoginRequest;
import com.bitreiver.app_server.domain.user.dto.UserResponse;
import com.bitreiver.app_server.domain.user.dto.UserSignUpRequest;
import com.bitreiver.app_server.domain.user.service.UserService;
import com.bitreiver.app_server.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "사용자 인증 및 관리 API")
public class UserController {
    
    private final UserService userService;
    
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
    public ApiResponse<AuthResponse> login(@Valid @RequestBody UserLoginRequest request) {
        AuthResponse response = userService.login(request);
        return ApiResponse.success(response, "로그인되었습니다.");
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
    public ApiResponse<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = userService.refreshToken(request.getRefreshToken());
        return ApiResponse.success(response, "토큰이 갱신되었습니다.");
    }
    
    @Operation(summary = "로그아웃", description = "사용자 로그아웃을 처리하고 토큰을 무효화합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "JWT")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(Authentication authentication, HttpServletRequest httpRequest, @RequestBody(required = false) LogoutRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        
        // 요청 바디에서 토큰을 받거나, Authorization 헤더에서 access token 추출
        String accessToken = null;
        String refreshToken = null;
        
        if (request != null) {
            accessToken = request.getAccessToken();
            refreshToken = request.getRefreshToken();
        } else {
            // Authorization 헤더에서 access token 추출 (필터에서 이미 검증됨)
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                accessToken = authHeader.substring(7);
            }
        }
        
        userService.logout(userId, accessToken, refreshToken);
        return ApiResponse.success(null, "로그아웃되었습니다.");
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
}

