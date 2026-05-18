CREATE TABLE IF NOT EXISTS ai_suggest_daily_usage (
    id BIGSERIAL PRIMARY KEY,
    actor_key VARCHAR(80) NOT NULL,
    usage_date DATE NOT NULL,
    used_count INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_ai_suggest_actor_date UNIQUE (actor_key, usage_date)
);

CREATE INDEX IF NOT EXISTS idx_ai_suggest_usage_date ON ai_suggest_daily_usage (usage_date);
