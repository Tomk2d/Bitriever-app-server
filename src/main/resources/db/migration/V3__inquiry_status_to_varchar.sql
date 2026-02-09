-- inquiry.status 컬럼을 PostgreSQL ENUM(inquiry_status)에서 VARCHAR(20)으로 변경
-- Hibernate @Enumerated(EnumType.STRING)과 호환되도록 함

ALTER TABLE inquiry
  ALTER COLUMN status TYPE varchar(20) USING status::text;
