CREATE TABLE analytics.champion_stats (
                                          puuid           TEXT NOT NULL,
                                          champion_name   TEXT NOT NULL,
                                          games_played    INT,
                                          wins            INT,
                                          losses          INT,
                                          avg_kda         NUMERIC(5,2),
                                          avg_cs          NUMERIC(6,2),
                                          avg_damage      INT,
                                          avg_vision      NUMERIC(5,2),
                                          last_computed   TIMESTAMPTZ,
                                          PRIMARY KEY (puuid, champion_name)
);

CREATE TABLE analytics.role_stats (
                                      puuid           TEXT NOT NULL,
                                      role            TEXT NOT NULL,
                                      games_played    INT,
                                      wins            INT,
                                      losses          INT,
                                      avg_kda         NUMERIC(5,2),
                                      last_computed   TIMESTAMPTZ,
                                      PRIMARY KEY (puuid, role)
);