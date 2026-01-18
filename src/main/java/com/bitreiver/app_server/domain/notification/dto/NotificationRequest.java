package com.bitreiver.app_server.domain.notification.dto;

import com.bitreiver.app_server.domain.notification.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Schema(
    description = "알림 생성 요청",
    example = "{\"type\": \"SYSTEM\", \"title\": \"새로운 알림\", \"content\": \"알림 내용입니다.\"}"
)
public class NotificationRequest {
    
    @Schema(description = "알림 타입", example = "SYSTEM", required = true, allowableValues = {"USER_ACTIVITY", "SYSTEM", "TRADING"})
    @NotNull(message = "알림 타입은 필수입니다.")
    private NotificationType type;
    
    @Schema(description = "알림 제목", example = "새로운 알림", required = true)
    @NotBlank(message = "제목은 필수입니다.")
    private String title;
    
    @Schema(description = "알림 내용", example = "알림 내용입니다.")
    private String content;
    
    @Schema(description = "추가 메타데이터 (JSON 형식, 선택사항)", example = "", hidden = true)
    private String metadata;
}
