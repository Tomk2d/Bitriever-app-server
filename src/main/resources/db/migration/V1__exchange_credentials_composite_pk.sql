-- exchange_credentials 복합 PK 마이그레이션 (사용자당 여러 거래소 지원)
-- 기존 테이블이 user_id 단일 PK인 경우에만 실행하세요.

-- 1. 기존 PK 제거 (제약명은 PostgreSQL 기본값 사용 시 exchange_credentials_pkey)
ALTER TABLE exchange_credentials DROP CONSTRAINT IF EXISTS exchange_credentials_pkey;

-- 2. exchange_provider NOT NULL 보장 (이미 있으면 무시)
ALTER TABLE exchange_credentials ALTER COLUMN exchange_provider SET NOT NULL;

-- 3. 복합 PK 추가
ALTER TABLE exchange_credentials ADD PRIMARY KEY (user_id, exchange_provider);
