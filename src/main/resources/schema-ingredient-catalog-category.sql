-- ingredient_catalog.category 컬럼 (DDL_AUTO가 none일 때 수동 적용)
ALTER TABLE ingredient_catalog ADD COLUMN IF NOT EXISTS category VARCHAR(80);
