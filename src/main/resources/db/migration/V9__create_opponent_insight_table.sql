CREATE TABLE coaching.opponent_insight (
                                           id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                           puuid           TEXT NOT NULL,
                                           game_id         TEXT NOT NULL,
                                           champion_name   TEXT,
                                           generated_at    TIMESTAMPTZ DEFAULT NOW(),
                                           content         TEXT NOT NULL,
                                           UNIQUE (puuid, game_id)
);

CREATE INDEX idx_opponent_insight_game_id ON coaching.opponent_insight(game_id);