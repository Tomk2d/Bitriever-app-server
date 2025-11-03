package com.bitreiver.app_server.domain.diary.dto;

import com.bitreiver.app_server.domain.diary.entity.Diary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "매매 일지 응답")
public class DiaryResponse {
    
    @Schema(description = "일지 ID", example = "1")
    private Integer id;
    
    @Schema(description = "매매 내역 ID", example = "1")
    private Integer tradingHistoryId;
    
    @Schema(description = "일지 내용 (JSONB 형식)", example = "{\"blocks\":[{\"type\":\"text\",\"content\":\"오늘 비트코인을 매수했습니다...\"}]}")
    private String content;
    
    @Schema(description = "태그 배열", example = "[\"스캘핑\", \"분석필요\"]")
    private List<String> tags;
    
    public static DiaryResponse from(Diary diary) {
        return DiaryResponse.builder()
            .id(diary.getId())
            .tradingHistoryId(diary.getTradingHistoryId())
            .content(diary.getContent())
            .tags(diary.getTags())
            .build();
    }
}

