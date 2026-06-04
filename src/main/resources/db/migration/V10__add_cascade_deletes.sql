ALTER TABLE ranked.entry
DROP CONSTRAINT entry_puuid_fkey,
    ADD CONSTRAINT entry_puuid_fkey
        FOREIGN KEY (puuid) REFERENCES account.summoner(puuid) ON DELETE CASCADE;

ALTER TABLE coaching.insight
DROP CONSTRAINT insight_puuid_fkey,
    ADD CONSTRAINT insight_puuid_fkey
        FOREIGN KEY (puuid) REFERENCES account.summoner(puuid) ON DELETE CASCADE;

ALTER TABLE coaching.insight_section
DROP CONSTRAINT insight_section_insight_id_fkey,
    ADD CONSTRAINT insight_section_insight_id_fkey
        FOREIGN KEY (insight_id) REFERENCES coaching.insight(id) ON DELETE CASCADE;

ALTER TABLE coaching.insight_match
DROP CONSTRAINT insight_match_insight_id_fkey,
    ADD CONSTRAINT insight_match_insight_id_fkey
        FOREIGN KEY (insight_id) REFERENCES coaching.insight(id) ON DELETE CASCADE;

ALTER TABLE coaching.insight_match
DROP CONSTRAINT insight_match_match_id_fkey,
    ADD CONSTRAINT insight_match_match_id_fkey
        FOREIGN KEY (match_id) REFERENCES match.summary(match_id) ON DELETE CASCADE;

ALTER TABLE match.participant
DROP CONSTRAINT participant_match_id_fkey,
    ADD CONSTRAINT participant_match_id_fkey
        FOREIGN KEY (match_id) REFERENCES match.summary(match_id) ON DELETE CASCADE;