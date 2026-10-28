ALTER TABLE characters
    ADD COLUMN cultivator_type VARCHAR(16) NOT NULL DEFAULT 'NAM_TU' AFTER name,
    ADD COLUMN spirit_type VARCHAR(20) NOT NULL DEFAULT 'THANH_LONG' AFTER cultivator_type;
