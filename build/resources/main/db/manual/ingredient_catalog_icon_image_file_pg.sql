-- ddl-auto 가 none 일 때 수동 실행 (PostgreSQL)
-- 시스템 카탈로그 아이콘 파일명(영문 등). 예: onion.png 또는 하위폴더/파일.png

ALTER TABLE ingredient_catalog
    ADD COLUMN IF NOT EXISTS icon_image_file VARCHAR(255);

-- 예: 기존 DB에 양파 행만 업데이트
-- UPDATE ingredient_catalog SET icon_image_file = 'onion.png'
-- WHERE user_id IS NULL AND name = '양파';
