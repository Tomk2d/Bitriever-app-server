CREATE TABLE assets (
    id SERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    exchange_code SMALLINT NOT NULL,
    coin_id INTEGER,
    symbol VARCHAR(20) NOT NULL,
    trade_by_symbol VARCHAR(10) NOT NULL,
    quantity NUMERIC(20, 8) NOT NULL DEFAULT 0,
    locked_quantity NUMERIC(20, 8) NOT NULL DEFAULT 0,
    avg_buy_price NUMERIC(20, 8) NOT NULL DEFAULT 0,
    avg_buy_price_modified BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_assets_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_assets_coin FOREIGN KEY (coin_id) REFERENCES coins(id) ON DELETE SET NULL,
    CONSTRAINT uk_assets_user_exchange_symbol UNIQUE (user_id, exchange_code, symbol, trade_by_symbol),
    CONSTRAINT chk_quantity_non_negative CHECK (quantity >= 0),
    CONSTRAINT chk_locked_quantity_non_negative CHECK (locked_quantity >= 0),
    CONSTRAINT chk_avg_buy_price_non_negative CHECK (avg_buy_price >= 0)
);

-- 기본 인덱스
CREATE INDEX idx_assets_user_id ON assets(user_id);
CREATE INDEX idx_assets_exchange_code ON assets(exchange_code);
CREATE INDEX idx_assets_coin_id ON assets(coin_id);
CREATE INDEX idx_assets_user_exchange ON assets(user_id, exchange_code);

-- 추가 인덱스 (조회 패턴 최적화)
CREATE INDEX idx_assets_user_symbol ON assets(user_id, symbol);
CREATE INDEX idx_assets_exchange_symbol ON assets(exchange_code, symbol);

-- updated_at 자동 업데이트 트리거
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_assets_updated_at 
    BEFORE UPDATE ON assets 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
