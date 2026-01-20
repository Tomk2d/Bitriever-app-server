-- 커뮤니티 게시글 좋아요/싫어요 테이블 생성
CREATE TABLE community_reactions (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    community_id INTEGER NOT NULL,
    reaction_type VARCHAR(10) NOT NULL CHECK (reaction_type IN ('LIKE', 'DISLIKE')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_community_reactions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_community_reactions_community FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE CASCADE,
    CONSTRAINT uk_community_reactions_user_community UNIQUE (user_id, community_id)
);

-- 기본 인덱스
CREATE INDEX idx_community_reactions_user_id ON community_reactions(user_id);
CREATE INDEX idx_community_reactions_community_id ON community_reactions(community_id);
CREATE INDEX idx_community_reactions_reaction_type ON community_reactions(reaction_type);

-- 복합 인덱스 (게시글별 반응 타입 집계 최적화)
CREATE INDEX idx_community_reactions_community_reaction ON community_reactions(community_id, reaction_type);

-- 테이블 코멘트
COMMENT ON TABLE community_reactions IS '커뮤니티 게시글 좋아요/싫어요 테이블';
COMMENT ON COLUMN community_reactions.id IS '반응 ID (PK)';
COMMENT ON COLUMN community_reactions.user_id IS '사용자 ID (FK to users)';
COMMENT ON COLUMN community_reactions.community_id IS '게시글 ID (FK to communities)';
COMMENT ON COLUMN community_reactions.reaction_type IS '반응 타입 (LIKE 또는 DISLIKE)';
COMMENT ON COLUMN community_reactions.created_at IS '생성일시';
COMMENT ON CONSTRAINT uk_community_reactions_user_community ON community_reactions IS '한 사용자가 한 게시글에 하나의 반응만 가능하도록 보장';
