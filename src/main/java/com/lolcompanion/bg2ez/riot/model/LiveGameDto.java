package com.lolcompanion.bg2ez.riot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LiveGameDto(
        Long gameId,
        String gameMode,
        Long gameLength,
        Integer gameQueueConfigId,
        List<LiveParticipantDto> participants
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LiveParticipantDto(
            Long teamId,
            String puuid,
            String summonerName,
            Integer championId,
            String riotId
    ) {}
}