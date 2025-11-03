package com.bitreiver.app_server.domain.exchange.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExchangeType {
    UPBIT(1, "UPBIT", "업비트"),
    BITHUMB(2, "BITHUMB", "빗썸"),
    COINONE(3, "COINONE", "코인원"),
    BINANCE(11, "BINANCE", "바이낸스"),
    BYBIT(12, "BYBIT", "바이빗"),
    COINBASE(13, "COINBASE", "코인베이스"),
    OKX(14, "OKX", "OKX");
    
    private final int code;
    private final String name;
    private final String koreanName;
    
    public static ExchangeType fromCode(int code) {
        for (ExchangeType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown exchange type code: " + code);
    }
    
    public static ExchangeType fromName(String name) {
        for (ExchangeType type : values()) {
            if (type.name.equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown exchange type name: " + name);
    }
}

