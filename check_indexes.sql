-- trading_histories 테이블의 인덱스 확인 쿼리

-- 1. 테이블 구조 및 인덱스 확인
\d trading_histories

-- 2. 인덱스 목록 확인
SELECT 
    indexname,
    indexdef
FROM 
    pg_indexes
WHERE 
    tablename = 'trading_histories'
ORDER BY 
    indexname;

-- 3. trade_time 컬럼에 인덱스가 있는지 확인
SELECT 
    i.indexname,
    i.indexdef,
    a.attname AS column_name
FROM 
    pg_indexes i
    JOIN pg_class c ON c.relname = i.indexname
    JOIN pg_index idx ON idx.indexrelid = c.oid
    JOIN pg_attribute a ON a.attrelid = idx.indrelid AND a.attnum = ANY(idx.indkey)
WHERE 
    i.tablename = 'trading_histories'
    AND a.attname = 'trade_time'
ORDER BY 
    i.indexname;

