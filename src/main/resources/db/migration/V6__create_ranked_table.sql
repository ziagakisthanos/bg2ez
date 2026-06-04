CREATE TABLE ranked.entry (
                              id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              puuid           TEXT NOT NULL REFERENCES account.summoner(puuid),
                              queue_type      TEXT NOT NULL,
                              tier            TEXT,
                              rank            TEXT,
                              league_points   INT,
                              wins            INT,
                              losses          INT,
                              recorded_at     TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_ranked_puuid ON ranked.entry(puuid);