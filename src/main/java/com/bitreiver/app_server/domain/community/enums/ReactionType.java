package com.bitreiver.app_server.domain.community.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReactionType {
    LIKE("LIKE", "좋아요"),
    DISLIKE("DISLIKE", "싫어요");
    
    private final String code;
    private final String koreanName;
    
    public static ReactionType fromCode(String code) {
        for (ReactionType reactionType : values()) {
            if (reactionType.code.equalsIgnoreCase(code)) {
                return reactionType;
            }
        }
        throw new IllegalArgumentException("Unknown reaction type code: " + code);
    }
}
