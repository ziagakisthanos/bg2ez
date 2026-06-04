CREATE TABLE coaching.insight (
                                  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  puuid               TEXT NOT NULL REFERENCES account.summoner(puuid),
                                  insight_type        TEXT NOT NULL,
                                  subject             TEXT,
                                  match_window        INT,
                                  generated_at        TIMESTAMPTZ DEFAULT NOW(),
                                  model_used          TEXT,
                                  prompt_tokens       INT,
                                  completion_tokens   INT
);

CREATE TABLE coaching.insight_section (
                                          id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                          insight_id      UUID NOT NULL REFERENCES coaching.insight(id),
                                          section_type    TEXT NOT NULL,
                                          content         TEXT NOT NULL,
                                          display_order   INT
);

CREATE TABLE coaching.insight_match (
                                        insight_id      UUID NOT NULL REFERENCES coaching.insight(id),
                                        match_id        TEXT NOT NULL REFERENCES match.summary(match_id),
                                        PRIMARY KEY (insight_id, match_id)
);

CREATE INDEX idx_insight_puuid ON coaching.insight(puuid);
CREATE INDEX idx_insight_section_insight_id ON coaching.insight_section(insight_id);