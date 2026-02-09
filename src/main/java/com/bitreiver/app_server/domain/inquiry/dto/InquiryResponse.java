package com.bitreiver.app_server.domain.inquiry.dto;

import com.bitreiver.app_server.domain.inquiry.entity.Inquiry;
import com.bitreiver.app_server.domain.inquiry.enums.InquiryStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "문의 응답")
public class InquiryResponse {

    @Schema(description = "문의 ID")
    private Long id;

    @Schema(description = "문의 내용")
    private String content;

    @Schema(description = "처리 상태 (NEW, IN_PROGRESS, DONE)")
    private InquiryStatus status;

    @Schema(description = "생성 시각")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public static InquiryResponse from(Inquiry inquiry) {
        return InquiryResponse.builder()
            .id(inquiry.getId())
            .content(inquiry.getContent())
            .status(inquiry.getStatus())
            .createdAt(inquiry.getCreatedAt())
            .build();
    }
}
