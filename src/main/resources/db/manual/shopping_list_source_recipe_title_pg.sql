-- 기존 DB에만 실행: 장바구니 항목에 레시피 출처 표시용 컬럼
ALTER TABLE shopping_list_items ADD COLUMN IF NOT EXISTS source_recipe_title VARCHAR(200);
