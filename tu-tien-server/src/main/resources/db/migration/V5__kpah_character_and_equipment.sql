-- Phase 1: KPAH Character Classes, Attributes, and Equipment Migration

-- 1. Thêm các trường chuẩn KPAH vào bảng characters
ALTER TABLE characters
    ADD COLUMN class_id TINYINT NOT NULL DEFAULT 0 AFTER account_id,
    ADD COLUMN gender TINYINT NOT NULL DEFAULT 1 AFTER class_id,
    ADD COLUMN head_id TINYINT NOT NULL DEFAULT 0 AFTER gender,
    ADD COLUMN nation_id TINYINT NOT NULL DEFAULT 0 AFTER head_id,
    ADD COLUMN element_id TINYINT NOT NULL DEFAULT 0 AFTER nation_id,
    ADD COLUMN strength SMALLINT NOT NULL DEFAULT 25 AFTER level,
    ADD COLUMN agility SMALLINT NOT NULL DEFAULT 20 AFTER strength,
    ADD COLUMN spirit SMALLINT NOT NULL DEFAULT 10 AFTER agility,
    ADD COLUMN health SMALLINT NOT NULL DEFAULT 25 AFTER spirit,
    ADD COLUMN luck SMALLINT NOT NULL DEFAULT 10 AFTER health,
    ADD COLUMN base_points SMALLINT NOT NULL DEFAULT 0 AFTER luck,
    ADD COLUMN skill_points SMALLINT NOT NULL DEFAULT 0 AFTER base_points,
    ADD COLUMN exp BIGINT NOT NULL DEFAULT 0 AFTER skill_points,
    ADD COLUMN hp INT NOT NULL DEFAULT 100 AFTER exp,
    ADD COLUMN max_hp INT NOT NULL DEFAULT 100 AFTER hp,
    ADD COLUMN mp INT NOT NULL DEFAULT 100 AFTER max_hp,
    ADD COLUMN max_mp INT NOT NULL DEFAULT 100 AFTER mp,
    ADD COLUMN xu BIGINT NOT NULL DEFAULT 10000 AFTER max_mp,
    ADD COLUMN luong INT NOT NULL DEFAULT 0 AFTER xu,
    ADD COLUMN luong_khoa INT NOT NULL DEFAULT 0 AFTER luong;

-- Cập nhật đồng bộ dữ liệu cũ nếu có
UPDATE characters
SET gender = CASE WHEN cultivator_type = 'NU_TU' THEN 2 ELSE 1 END;

-- 2. Mở rộng ràng buộc cho equipment_templates và character_equipment để hỗ trợ Vũ khí (WEAPON)
ALTER TABLE equipment_templates DROP CONSTRAINT IF EXISTS chk_equipment_slot;
ALTER TABLE equipment_templates DROP CONSTRAINT IF EXISTS chk_equipment_gender;
ALTER TABLE character_equipment DROP CONSTRAINT IF EXISTS chk_character_equipment_slot;

-- Thêm 5 vũ khí tân thủ KPAH vào equipment_templates
INSERT INTO equipment_templates (template_id, name, slot_code, sprite_style, icon_id, gender_type, required_level, attributes_json) VALUES
    (79, 'Kiếm tre', 'WEAPON', 0, 0, 'ALL', 1, '[30,0,0,1,0,0,0,0,0,0]'),
    (86, 'Đao tre', 'WEAPON', 0, 5, 'ALL', 1, '[50,0,0,1,0,0,0,0,0,0]'),
    (93, 'Bút tre', 'WEAPON', 0, 10, 'ALL', 1, '[70,0,0,0,0,0,0,0,0,0]'),
    (100, 'Thổ búa', 'WEAPON', 0, 15, 'ALL', 1, '[90,0,0,0,0,0,0,0,0,0]'),
    (107, 'Cung tre', 'WEAPON', 0, 20, 'ALL', 1, '[20,0,0,5,1,0,0,0,0,0]')
ON DUPLICATE KEY UPDATE name = VALUES(name);
