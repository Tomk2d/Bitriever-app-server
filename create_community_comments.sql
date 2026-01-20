-- 커뮤니티 게시글 댓글 테이블 생성
CREATE TABLE community_comments (
    id BIGSERIAL PRIMARY KEY,
    community_id INTEGER NOT NULL,
    user_id UUID NOT NULL,
    parent_id BIGINT,
    content TEXT NOT NULL,
    deleted BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_community_comments_community FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE CASCADE,
    CONSTRAINT fk_community_comments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_community_comments_parent FOREIGN KEY (parent_id) REFERENCES community_comments(id) ON DELETE CASCADE,
    CONSTRAINT chk_content_not_empty CHECK (char_length(trim(content)) > 0)
);

-- 기본 인덱스
CREATE INDEX idx_community_comments_community_id ON community_comments(community_id);
CREATE INDEX idx_community_comments_parent_id ON community_comments(parent_id);
CREATE INDEX idx_community_comments_user_id ON community_comments(user_id);
CREATE INDEX idx_community_comments_created_at ON community_comments(created_at DESC);

-- 복합 인덱스 (게시글별 최상위 댓글 조회 최적화)
CREATE INDEX idx_community_comments_community_parent ON community_comments(community_id, parent_id) WHERE parent_id IS NULL;

-- 사용자별 댓글 조회 최적화
CREATE INDEX idx_community_comments_user_created_at ON community_comments(user_id, created_at DESC);

-- updated_at 자동 업데이트 트리거
CREATE TRIGGER update_community_comments_updated_at 
    BEFORE UPDATE ON community_comments 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- 테이블 코멘트
COMMENT ON TABLE community_comments IS '커뮤니티 게시글 댓글 테이블';
COMMENT ON COLUMN community_comments.id IS '댓글 ID (PK)';
COMMENT ON COLUMN community_comments.community_id IS '게시글 ID (FK to communities)';
COMMENT ON COLUMN community_comments.user_id IS '작성자 ID (FK to users)';
COMMENT ON COLUMN community_comments.parent_id IS '부모 댓글 ID (FK to community_comments, null이면 최상위 댓글)';
COMMENT ON COLUMN community_comments.content IS '댓글 내용';
COMMENT ON COLUMN community_comments.deleted IS '삭제 여부 (soft delete)';
COMMENT ON COLUMN community_comments.created_at IS '생성일시';
COMMENT ON COLUMN community_comments.updated_at IS '수정일시';
