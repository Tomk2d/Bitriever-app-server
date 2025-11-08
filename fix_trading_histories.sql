-- trading_histories 테이블에 칼럼 추가
ALTER TABLE trading_histories
ADD COLUMN profit_loss_rate NUMERIC(5, 2) NULL,
ADD COLUMN avg_buy_price NUMERIC(20, 8) NULL;

-- 코멘트 추가
COMMENT ON COLUMN trading_histories.profit_loss_rate IS '상승하락률 (50% 상승 = 0.50, 50% 하락 = -0.50)';
COMMENT ON COLUMN trading_histories.avg_buy_price IS '구매 시 평균 단가 (매도 시에만 값 존재)';