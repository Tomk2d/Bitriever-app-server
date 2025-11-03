-- journals 테이블 삭제 쿼리

-- 1. 외래키 제약조건이 있는 경우 CASCADE로 모든 의존성과 함께 삭제
DROP TABLE IF EXISTS journals CASCADE;

-- 2. 또는 안전하게 단계별로 삭제 (권장)
-- 먼저 외래키 확인 후 삭제
-- DROP TABLE IF EXISTS journals;

-- 3. 인덱스가 별도로 생성된 경우 (테이블 삭제 시 자동 삭제되지만 명시적으로)
-- DROP INDEX IF EXISTS idx_journals_user_id;
-- DROP INDEX IF EXISTS idx_journals_trading_history_id;

