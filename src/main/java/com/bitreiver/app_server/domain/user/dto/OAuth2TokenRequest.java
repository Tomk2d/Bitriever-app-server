package com.bitreiver.app_server.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OAuth2 code 교환 요청")
public class OAuth2TokenRequest {
    @NotBlank(message = "code는 필수입니다.")
    @Schema(description = "OAuth2 성공 시 발급된 일회성 code", required = true)
    private String code;
}
