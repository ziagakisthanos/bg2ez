package com.lolcompanion.bg2ez.ranked.model;

import java.time.OffsetDateTime;

public record RankedEntryModel(
        String queueType,
        String tier,
        String rank,
        Integer leaguePoints,
        Integer wins,
        Integer losses,
        OffsetDateTime recordedAt
) {}