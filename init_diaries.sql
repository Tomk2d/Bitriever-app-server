-- 기존 거래 내역에 대한 일지 초기 데이터 생성
-- content와 tags는 NULL로 뼈대만 생성

INSERT INTO diaries (trading_history_id, content, tags)
SELECT 
    id AS trading_history_id,
    NULL AS content,
    NULL AS tags
FROM trading_histories
WHERE id NOT IN (
    SELECT trading_history_id 
    FROM diaries 
    WHERE trading_history_id IS NOT NULL
)
ON CONFLICT (trading_history_id) DO NOTHING;

-- 생성된 일지 개수 확인
SELECT COUNT(*) AS created_diaries_count FROM diaries;

