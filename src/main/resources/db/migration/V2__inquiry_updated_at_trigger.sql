-- inquiry.updated_at 자동 갱신 트리거 (PostgreSQL)
-- 사전 조건: inquiry 테이블이 존재해야 하며, user_id는 이 프로젝트 기준 users(id)와 동일한 타입(UUID)이어야 합니다.

CREATE OR REPLACE FUNCTION set_inquiry_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS inquiry_updated_at_trigger ON inquiry;
CREATE TRIGGER inquiry_updated_at_trigger
  BEFORE UPDATE ON inquiry
  FOR EACH ROW
  EXECUTE PROCEDURE set_inquiry_updated_at();
