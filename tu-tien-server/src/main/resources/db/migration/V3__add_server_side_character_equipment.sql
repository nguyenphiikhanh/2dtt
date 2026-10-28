CREATE TABLE equipment_templates (
    template_id SMALLINT NOT NULL,
    name VARCHAR(80) NOT NULL,
    slot_code VARCHAR(16) NOT NULL,
    sprite_style SMALLINT NOT NULL,
    icon_id SMALLINT NOT NULL,
    gender_type VARCHAR(16) NOT NULL,
    required_level INT NOT NULL DEFAULT 1,
    attributes_json JSON NOT NULL,
    PRIMARY KEY (template_id),
    CONSTRAINT chk_equipment_slot CHECK (slot_code IN ('SHIRT', 'PANTS')),
    CONSTRAINT chk_equipment_gender CHECK (gender_type IN ('NAM_TU', 'NU_TU'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO equipment_templates (template_id, name, slot_code, sprite_style, icon_id, gender_type, required_level, attributes_json) VALUES
    (1, 'Áo bà ba', 'SHIRT', 1, 176, 'NU_TU', 1, '[0,10,1,1,0,0,10,0,0,0]'),
    (2, 'Áo nhập môn', 'SHIRT', 0, 177, 'NAM_TU', 1, '[0,10,1,1,0,0,10,0,0,0]'),
    (3, 'Áo đai thô nữ', 'SHIRT', 3, 178, 'NU_TU', 4, '[0,15,1,2,0,0,15,0,0,0]'),
    (4, 'Áo đai thô nam', 'SHIRT', 2, 179, 'NAM_TU', 4, '[0,15,1,2,0,0,15,0,0,0]'),
    (27, 'Quần bà ba', 'PANTS', 1, 202, 'NU_TU', 1, '[0,7,0,0,0,0,7,0,0,0]'),
    (28, 'Quần nhập môn', 'PANTS', 0, 203, 'NAM_TU', 1, '[0,7,0,0,0,0,7,0,0,0]'),
    (29, 'Quần đai thô nữ', 'PANTS', 3, 204, 'NU_TU', 4, '[0,12,0,1,0,0,12,0,0,0]'),
    (30, 'Quần đai thô nam', 'PANTS', 2, 205, 'NAM_TU', 4, '[0,12,0,1,0,0,12,0,0,0]');

CREATE TABLE character_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    character_id BIGINT NOT NULL,
    template_id SMALLINT NOT NULL,
    durability INT NOT NULL DEFAULT 100,
    enhancement_level INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_character_item_owner UNIQUE (character_id, id),
    CONSTRAINT fk_character_items_character FOREIGN KEY (character_id)
        REFERENCES characters (id) ON DELETE CASCADE,
    CONSTRAINT fk_character_items_template FOREIGN KEY (template_id)
        REFERENCES equipment_templates (template_id),
    INDEX idx_character_items_owner (character_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE character_equipment (
    character_id BIGINT NOT NULL,
    slot_code VARCHAR(16) NOT NULL,
    item_instance_id BIGINT NOT NULL,
    equipped_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (character_id, slot_code),
    CONSTRAINT uq_character_equipment_item UNIQUE (character_id, item_instance_id),
    CONSTRAINT chk_character_equipment_slot CHECK (slot_code IN ('SHIRT', 'PANTS')),
    CONSTRAINT fk_character_equipment_owner FOREIGN KEY (character_id)
        REFERENCES characters (id) ON DELETE CASCADE,
    CONSTRAINT fk_character_equipment_item FOREIGN KEY (character_id, item_instance_id)
        REFERENCES character_items (character_id, id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO character_items (character_id, template_id)
SELECT id, CASE WHEN cultivator_type = 'NU_TU' THEN 1 ELSE 2 END FROM characters;
INSERT INTO character_items (character_id, template_id)
SELECT id, CASE WHEN cultivator_type = 'NU_TU' THEN 27 ELSE 28 END FROM characters;
INSERT INTO character_equipment (character_id, slot_code, item_instance_id)
SELECT i.character_id, t.slot_code, i.id
FROM character_items i
JOIN equipment_templates t ON t.template_id = i.template_id;
