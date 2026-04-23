-- ddl-auto 가 none 일 때 수동 실행 (PostgreSQL)

CREATE TABLE IF NOT EXISTS user_ingredient_images (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    ingredient_name VARCHAR(120) NOT NULL,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    content_type VARCHAR(80) NOT NULL,
    file_extension VARCHAR(10) NOT NULL,
    CONSTRAINT uq_user_ingredient UNIQUE (user_id, ingredient_name)
);

CREATE INDEX IF NOT EXISTS idx_user_ingredient_images_user ON user_ingredient_images (user_id);
