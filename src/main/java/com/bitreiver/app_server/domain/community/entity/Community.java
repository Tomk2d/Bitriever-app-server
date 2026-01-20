package com.bitreiver.app_server.domain.community.entity;

import com.bitreiver.app_server.domain.community.enums.Category;
import com.bitreiver.app_server.domain.community.converter.CategoryConverter;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "communities", indexes = {
    @Index(name = "idx_communities_user_id", columnList = "user_id"),
    @Index(name = "idx_communities_category", columnList = "category"),
    @Index(name = "idx_communities_created_at", columnList = "created_at"),
    @Index(name = "idx_communities_category_created_at", columnList = "category, created_at"),
    @Index(name = "idx_communities_user_created_at", columnList = "user_id, created_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Community {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Convert(converter = CategoryConverter.class)
    @Column(name = "category", nullable = false, length = 50)
    private Category category;
    
    @Column(name = "title", nullable = false, length = 500)
    private String title;
    
    @Type(JsonType.class)
    @Column(name = "content", columnDefinition = "jsonb")
    private String content;
    
    @Column(name = "hashtags", columnDefinition = "text[]")
    private List<String> hashtags;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
