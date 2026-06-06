CREATE TABLE match.timeline_event (
                                      id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                      match_id        TEXT NOT NULL REFERENCES match.summary(match_id) ON DELETE CASCADE,
                                      event_type      TEXT NOT NULL,
                                      timestamp_ms    BIGINT NOT NULL,
                                      killer_puuid    TEXT,
                                      victim_puuid    TEXT,
                                      position_x      INT,
                                      position_y      INT,
                                      map_zone        TEXT,
                                      is_early_game   BOOLEAN GENERATED ALWAYS AS (timestamp_ms < 600000) STORED,
                                      assisting_puuids TEXT[]
);

CREATE INDEX idx_timeline_match_id ON match.timeline_event(match_id);
CREATE INDEX idx_timeline_killer_puuid ON match.timeline_event(killer_puuid);
CREATE INDEX idx_timeline_victim_puuid ON match.timeline_event(victim_puuid);
CREATE INDEX idx_timeline_event_type ON match.timeline_event(event_type);