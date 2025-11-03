package com.bitreiver.app_server.domain.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "매매 일지 요청")
public class DiaryRequest {
    
    @Schema(description = "매매 내역 ID", example = "1", required = true)
    @NotNull(message = "매매 내역 ID는 필수입니다.")
    private Integer tradingHistoryId;
    
    @Schema(description = "일지 내용 (JSONB 형식)", example = "{\"blocks\":[{\"type\":\"text\",\"content\":\"오늘 비트코인을 매수했습니다...\"}]}")
    private String content;
    
    @Schema(description = "태그 배열", example = "[\"스캘핑\", \"분석필요\"]")
    private List<String> tags;
}

