-- 커뮤니티 댓글 좋아요/싫어요 테이블 생성
CREATE TABLE community_comment_reactions (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    comment_id BIGINT NOT NULL,
    reaction_type VARCHAR(10) NOT NULL CHECK (reaction_type IN ('LIKE', 'DISLIKE')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_community_comment_reactions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_community_comment_reactions_comment FOREIGN KEY (comment_id) REFERENCES community_comments(id) ON DELETE CASCADE,
    CONSTRAINT uk_community_comment_reactions_user_comment UNIQUE (user_id, comment_id)
);

-- 기본 인덱스
CREATE INDEX idx_community_comment_reactions_user_id ON community_comment_reactions(user_id);
CREATE INDEX idx_community_comment_reactions_comment_id ON community_comment_reactions(comment_id);
CREATE INDEX idx_community_comment_reactions_reaction_type ON community_comment_reactions(reaction_type);

-- 복합 인덱스 (댓글별 반응 타입 집계 최적화)
CREATE INDEX idx_community_comment_reactions_comment_reaction ON community_comment_reactions(comment_id, reaction_type);

-- 테이블 코멘트
COMMENT ON TABLE community_comment_reactions IS '커뮤니티 댓글 좋아요/싫어요 테이블';
COMMENT ON COLUMN community_comment_reactions.id IS '반응 ID (PK)';
COMMENT ON COLUMN community_comment_reactions.user_id IS '사용자 ID (FK to users)';
COMMENT ON COLUMN community_comment_reactions.comment_id IS '댓글 ID (FK to community_comments)';
COMMENT ON COLUMN community_comment_reactions.reaction_type IS '반응 타입 (LIKE 또는 DISLIKE)';
COMMENT ON COLUMN community_comment_reactions.created_at IS '생성일시';
COMMENT ON CONSTRAINT uk_community_comment_reactions_user_comment ON community_comment_reactions IS '한 사용자가 한 댓글에 하나의 반응만 가능하도록 보장';
