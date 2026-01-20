package com.bitreiver.app_server.domain.community.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Category {
    FREE("FREE", "자유"),
    GOOD_BAD_NEWS("GOOD_BAD_NEWS", "호재/악재"),
    PROFIT_PROOF("PROFIT_PROOF", "손익인증"),
    CHART_ANALYSIS("CHART_ANALYSIS", "차트분석"),
    NEWS("NEWS", "뉴스");
    
    private final String code;
    private final String koreanName;
    
    public static Category fromCode(String code) {
        for (Category category : values()) {
            if (category.code.equalsIgnoreCase(code)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown category code: " + code);
    }
}
