package com.lolcompanion.bg2ez.riot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MatchDetailDto(
        MatchMetadataDto metadata,
        MatchInfoDto info
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MatchMetadataDto(String matchId) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MatchInfoDto(
            Long gameStartTimestamp,
            Long gameDuration,
            Integer queueId,
            String gameMode,
            List<ParticipantDto> participants
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ParticipantDto(
            Integer participantId,
            String puuid,
            Integer championId,
            String championName,
            Integer kills,
            Integer deaths,
            Integer assists,
            Boolean win,
            Integer totalDamageDealtToChampions,
            Integer goldEarned,
            Integer totalMinionsKilled,
            Integer visionScore,
            String teamPosition,
            String lane,
            Integer turretKills
    ) {}
}
