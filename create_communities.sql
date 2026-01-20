-- 커뮤니티 게시글 테이블 생성
CREATE TABLE communities (
    id SERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    category VARCHAR(50) NOT NULL,
    title VARCHAR(500) NOT NULL,
    content JSONB,
    hashtags TEXT[] DEFAULT '{}',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_communities_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_title_not_empty CHECK (char_length(trim(title)) > 0)
);

-- 기본 인덱스
CREATE INDEX idx_communities_user_id ON communities(user_id);
CREATE INDEX idx_communities_category ON communities(category);
CREATE INDEX idx_communities_created_at ON communities(created_at DESC);

-- 해시태그 검색을 위한 GIN 인덱스 (배열 검색 최적화)
CREATE INDEX idx_communities_hashtags_gin ON communities USING GIN(hashtags);

-- 복합 인덱스 (카테고리별 최신순 조회 최적화)
CREATE INDEX idx_communities_category_created_at ON communities(category, created_at DESC);

-- 사용자별 게시글 조회 최적화
CREATE INDEX idx_communities_user_created_at ON communities(user_id, created_at DESC);

-- updated_at 자동 업데이트 트리거
CREATE TRIGGER update_communities_updated_at 
    BEFORE UPDATE ON communities 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- 테이블 코멘트
COMMENT ON TABLE communities IS '커뮤니티 게시글 테이블';
COMMENT ON COLUMN communities.id IS '게시글 ID (PK)';
COMMENT ON COLUMN communities.user_id IS '작성자 ID (FK to users)';
COMMENT ON COLUMN communities.category IS '카테고리 (FREE, QUESTION, TIP, NEWS 등)';
COMMENT ON COLUMN communities.title IS '게시글 제목';
COMMENT ON COLUMN communities.content IS '게시글 내용 (JSONB 형식: blocks 배열로 텍스트/이미지 순서 보존)';
COMMENT ON COLUMN communities.hashtags IS '해시태그 배열 (GIN 인덱스로 검색 최적화)';
COMMENT ON COLUMN communities.created_at IS '생성일시';
COMMENT ON COLUMN communities.updated_at IS '수정일시';
