package com.bitreiver.app_server.domain.user.dto;

import com.bitreiver.app_server.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String email;
    private String nickname;
    private Short signupType;
    private LocalDateTime createdAt;
    private Boolean isActive;
    private Boolean isConnectExchange;
    
    public static UserResponse from(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .signupType(user.getSignupType())
            .createdAt(user.getCreatedAt())
            .isActive(user.getIsActive())
            .isConnectExchange(user.getIsConnectExchange())
            .build();
    }
}

