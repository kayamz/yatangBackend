-- PostgreSQL: AI 레시피 연동 — 장바구니 · 레시피북 테이블
-- ddl-auto 가 none 인 경우 수동 실행하세요.

CREATE TABLE IF NOT EXISTS shopping_list_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    ingredient_name VARCHAR(200) NOT NULL,
    quantity_note VARCHAR(80),
    unit VARCHAR(40),
    source_recipe_title VARCHAR(200),
    checked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_shopping_list_user ON shopping_list_items (user_id);

CREATE TABLE IF NOT EXISTS saved_recipes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    servings INTEGER,
    cook_minutes INTEGER,
    payload_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_saved_recipes_user ON saved_recipes (user_id);
