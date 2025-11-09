package com.bitreiver.app_server.domain.diary.entity;

import com.bitreiver.app_server.domain.diary.enums.TradingMind;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.List;

@Entity
@Table(name = "diaries", indexes = {
    @Index(name = "idx_diaries_trading_history_id", columnList = "trading_history_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Diary {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "trading_history_id", nullable = false, unique = true)
    private Integer tradingHistoryId;
    
    @Type(JsonType.class)
    @Column(name = "content", columnDefinition = "jsonb")
    private String content;
    
    @Column(name = "tags", columnDefinition = "text[]")
    private List<String> tags;
    
    @Convert(converter = com.bitreiver.app_server.domain.diary.converter.TradingMindConverter.class)
    @Column(name = "trading_mind")
    private TradingMind tradingMind;
}

