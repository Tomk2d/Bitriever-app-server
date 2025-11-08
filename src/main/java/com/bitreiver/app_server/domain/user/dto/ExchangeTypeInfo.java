package com.bitreiver.app_server.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "거래소 정보")
public class ExchangeTypeInfo {
    @Schema(description = "거래소 코드", example = "1")
    private Integer code;
    
    @Schema(description = "거래소 이름", example = "UPBIT")
    private String name;
    
    @Schema(description = "거래소 한글 이름", example = "업비트")
    private String koreanName;
}

