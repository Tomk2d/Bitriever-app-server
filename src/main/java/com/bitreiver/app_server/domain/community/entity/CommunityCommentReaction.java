package com.bitreiver.app_server.domain.community.entity;

import com.bitreiver.app_server.domain.community.enums.ReactionType;
import com.bitreiver.app_server.domain.community.converter.ReactionTypeConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "community_comment_reactions", indexes = {
    @Index(name = "idx_community_comment_reactions_user_id", columnList = "user_id"),
    @Index(name = "idx_community_comment_reactions_comment_id", columnList = "comment_id"),
    @Index(name = "idx_community_comment_reactions_reaction_type", columnList = "reaction_type"),
    @Index(name = "idx_community_comment_reactions_comment_reaction", columnList = "comment_id, reaction_type")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_community_comment_reactions_user_comment", columnNames = {"user_id", "comment_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityCommentReaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "comment_id", nullable = false)
    private Long commentId;
    
    @Convert(converter = ReactionTypeConverter.class)
    @Column(name = "reaction_type", nullable = false, length = 10)
    private ReactionType reactionType;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
