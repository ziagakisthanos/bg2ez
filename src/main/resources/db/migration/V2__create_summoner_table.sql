CREATE TABLE account.summoner (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  puuid           TEXT UNIQUE NOT NULL,
  summoner_id     TEXT,
  game_name       TEXT NOT NULL,
  tag_line        TEXT NOT NULL,
  profile_icon_id INT,
  summoner_level  INT,
  linked_at       TIMESTAMPTZ DEFAULT NOW(),
  last_synced_at  TIMESTAMPTZ
);