package com.bitreiver.app_server.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
@Schema(description = "프로필 이미지 변경 요청")
public class SetProfileUrlRequest {

    private static final String ALLOWED_PATTERN = "^/profile[1-6]$";

    @Schema(description = "프로필 URL (/profile1 ~ /profile6)", example = "/profile2", required = true)
    @NotBlank(message = "프로필을 선택해주세요.")
    @Pattern(regexp = ALLOWED_PATTERN, message = "올바른 프로필을 선택해주세요.")
    private String profileUrl;
}
