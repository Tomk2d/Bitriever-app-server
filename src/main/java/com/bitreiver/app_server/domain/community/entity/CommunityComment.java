package com.bitreiver.app_server.domain.community.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "community_comments", indexes = {
    @Index(name = "idx_community_comments_community_id", columnList = "community_id"),
    @Index(name = "idx_community_comments_parent_id", columnList = "parent_id"),
    @Index(name = "idx_community_comments_user_id", columnList = "user_id"),
    @Index(name = "idx_community_comments_created_at", columnList = "created_at"),
    @Index(name = "idx_community_comments_community_parent", columnList = "community_id, parent_id"),
    @Index(name = "idx_community_comments_user_created_at", columnList = "user_id, created_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityComment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "community_id", nullable = false)
    private Integer communityId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "parent_id")
    private Long parentId;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
