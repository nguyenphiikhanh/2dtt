ALTER TABLE characters
    ADD COLUMN current_map_id INT NOT NULL DEFAULT 105 AFTER position_y;

UPDATE characters
SET position_x = 184, position_y = 280
WHERE position_x = 0 AND position_y = 0;
