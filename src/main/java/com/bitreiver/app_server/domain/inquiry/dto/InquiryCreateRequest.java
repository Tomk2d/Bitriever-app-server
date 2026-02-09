package com.bitreiver.app_server.domain.inquiry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "문의 생성 요청")
public class InquiryCreateRequest {

    @Schema(description = "문의 내용", example = "차트에 데이터가 이상하게 표시됩니다.", required = true)
    @NotBlank(message = "문의 내용을 입력해 주세요.")
    private String content;
}
