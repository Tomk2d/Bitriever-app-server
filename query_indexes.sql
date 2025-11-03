-- trading_histories 테이블의 모든 인덱스 조회

-- 방법 1: 간단한 인덱스 목록
SELECT 
    indexname,
    indexdef
FROM 
    pg_indexes
WHERE 
    tablename = 'trading_histories'
ORDER BY 
    indexname;

-- 방법 2: 상세한 인덱스 정보 (인덱스 타입, 컬럼 포함)
SELECT 
    i.indexname,
    i.indexdef,
    am.amname AS index_type,
    pg_size_pretty(pg_relation_size(i.indexname::regclass)) AS index_size
FROM 
    pg_indexes i
    JOIN pg_class c ON c.relname = i.indexname
    JOIN pg_am am ON am.oid = c.relam
WHERE 
    i.tablename = 'trading_histories'
ORDER BY 
    i.indexname;

-- 방법 3: 인덱스와 포함된 컬럼 정보
SELECT 
    i.indexname,
    i.indexdef,
    a.attname AS column_name,
    a.attnum AS column_position
FROM 
    pg_indexes i
    JOIN pg_class ic ON ic.relname = i.indexname
    JOIN pg_index idx ON idx.indexrelid = ic.oid
    JOIN pg_class tc ON tc.relname = i.tablename
    JOIN pg_attribute a ON a.attrelid = tc.oid AND a.attnum = ANY(idx.indkey)
WHERE 
    i.tablename = 'trading_histories'
ORDER BY 
    i.indexname, a.attnum;

-- 방법 4: 인덱스 사용 통계 (ANALYZE 후 실행)
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan AS index_scans,
    idx_tup_read AS tuples_read,
    idx_tup_fetch AS tuples_fetched
FROM 
    pg_stat_user_indexes
WHERE 
    tablename = 'trading_histories'
ORDER BY 
    idx_scan DESC;

