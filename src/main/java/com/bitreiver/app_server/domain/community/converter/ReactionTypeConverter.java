package com.bitreiver.app_server.domain.community.converter;

import com.bitreiver.app_server.domain.community.enums.ReactionType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ReactionTypeConverter implements AttributeConverter<ReactionType, String> {
    
    @Override
    public String convertToDatabaseColumn(ReactionType reactionType) {
        if (reactionType == null) {
            return null;
        }
        return reactionType.getCode();
    }
    
    @Override
    public ReactionType convertToEntityAttribute(String code) {
        if (code == null) {
            return null;
        }
        return ReactionType.fromCode(code);
    }
}
