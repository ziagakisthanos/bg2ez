ALTER TABLE analytics.champion_stats
    ADD COLUMN IF NOT EXISTS roam_kills        NUMERIC(5,2),
    ADD COLUMN IF NOT EXISTS early_kills       NUMERIC(5,2),
    ADD COLUMN IF NOT EXISTS obj_participation NUMERIC(5,2),
    ADD COLUMN IF NOT EXISTS death_zone        TEXT,
    ADD COLUMN IF NOT EXISTS comeback_kills    NUMERIC(5,2),
    ADD COLUMN IF NOT EXISTS split_push_kills  NUMERIC(5,2),
    ADD COLUMN IF NOT EXISTS wards_placed    NUMERIC(5,2),
    ADD COLUMN IF NOT EXISTS wards_destroyed NUMERIC(5,2);