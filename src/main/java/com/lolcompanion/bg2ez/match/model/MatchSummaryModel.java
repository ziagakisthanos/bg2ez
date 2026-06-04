package com.lolcompanion.bg2ez.match.model;

import java.time.OffsetDateTime;

public record MatchSummaryModel(
        String matchId,
        String gameMode,
        Integer gameDuration,
        OffsetDateTime gameStart,
        Integer queueId,
        String queueLabel,
        MatchParticipantModel player
) {}