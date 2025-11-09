package com.bitreiver.app_server.domain.diary.converter;

import com.bitreiver.app_server.domain.diary.enums.TradingMind;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TradingMindConverter implements AttributeConverter<TradingMind, Integer> {
    
    @Override
    public Integer convertToDatabaseColumn(TradingMind tradingMind) {
        if (tradingMind == null) {
            return null;
        }
        return tradingMind.getCode();
    }
    
    @Override
    public TradingMind convertToEntityAttribute(Integer code) {
        if (code == null) {
            return null;
        }
        return TradingMind.fromCode(code);
    }
}

