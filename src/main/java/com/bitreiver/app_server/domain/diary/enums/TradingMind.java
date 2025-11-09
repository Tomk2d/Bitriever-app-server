package com.bitreiver.app_server.domain.diary.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TradingMind {
    MINDLESS(0, "MINDLESS", "무념무상"),
    CONFIDENT(1, "CONFIDENT", "확신"),
    SOMEWHAT_CONFIDENT(2, "SOMEWHAT_CONFIDENT", "약간 확신"),
    EXPECTATION(3, "EXPECTATION", "기대감"),
    GREED(11, "GREED", "욕심"),
    IMPATIENCE(12, "IMPATIENCE", "조급함"),
    ANXIETY(13, "ANXIETY", "불안"),
    FEAR(14, "FEAR", "두려움");
    
    private final int code;
    private final String name;
    private final String koreanName;
    
    public static TradingMind fromCode(int code) {
        for (TradingMind mind : values()) {
            if (mind.code == code) {
                return mind;
            }
        }
        throw new IllegalArgumentException("Unknown trading mind code: " + code);
    }
    
    public static TradingMind fromName(String name) {
        for (TradingMind mind : values()) {
            if (mind.name.equalsIgnoreCase(name)) {
                return mind;
            }
        }
        throw new IllegalArgumentException("Unknown trading mind name: " + name);
    }
}

