package com.bitreiver.app_server.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "로그인 요청")
public class UserLoginRequest {
    
    @Schema(description = "사용자 이메일", example = "test0000@test.com", required = true)
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;
    
    @Schema(description = "비밀번호", example = "test1234", required = true)
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}

