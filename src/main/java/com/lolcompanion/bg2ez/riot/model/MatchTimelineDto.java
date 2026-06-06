package com.lolcompanion.bg2ez.riot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MatchTimelineDto(
        MatchTimelineMetadataDto metadata,
        MatchTimelineInfoDto info
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MatchTimelineMetadataDto(String matchId) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MatchTimelineInfoDto(
            List<FrameDto> frames
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FrameDto(
            Long timestamp,
            List<EventDto> events,
            Map<String, ParticipantFrameDto> participantFrames
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EventDto(
            String type,
            Long timestamp,
            String killerId,
            String victimId,
            List<String> assistingParticipantIds,
            PositionDto position,
            String wardType,
            String monsterType
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PositionDto(Integer x, Integer y) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ParticipantFrameDto(
            Integer participantId,
            Integer totalGold,
            Integer currentGold
    ) {}
}