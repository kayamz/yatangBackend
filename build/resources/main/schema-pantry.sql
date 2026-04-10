-- 상온보관(pantry_items). DDL_AUTO가 none일 때 수동 적용용 (PostgreSQL 예시)
CREATE TABLE IF NOT EXISTS pantry_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    quantity INTEGER NOT NULL,
    unit VARCHAR(20) NOT NULL,
    expiration_date DATE,
    manufacture_date DATE,
    memo VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    user_id BIGINT NOT NULL REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_pantry_items_user_id ON pantry_items (user_id);
