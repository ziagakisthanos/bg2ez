CREATE TABLE match.summary (
                               match_id        TEXT PRIMARY KEY,
                               game_mode       TEXT,
                               game_duration   INT,
                               game_start      TIMESTAMPTZ,
                               queue_id        INT
);

CREATE TABLE match.participant (
                                   id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                   match_id            TEXT NOT NULL REFERENCES match.summary(match_id),
                                   puuid               TEXT NOT NULL,
                                   champion_id         INT,
                                   champion_name       TEXT,
                                   kills               INT,
                                   deaths              INT,
                                   assists             INT,
                                   win                 BOOLEAN,
                                   total_damage        INT,
                                   gold_earned         INT,
                                   cs                  INT,
                                   vision_score        INT,
                                   role                TEXT,
                                   lane                TEXT,
                                   kill_participation  NUMERIC(5,2),
                                   solo_kills          INT,
                                   turret_damage       INT
);

CREATE INDEX idx_participant_puuid ON match.participant(puuid);
CREATE INDEX idx_participant_match_id ON match.participant(match_id);
CREATE INDEX idx_summary_game_start ON match.summary(game_start);