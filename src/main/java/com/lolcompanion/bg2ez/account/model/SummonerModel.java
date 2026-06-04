package com.lolcompanion.bg2ez.account.model;

import java.time.OffsetDateTime;

public record SummonerModel(
        String gameName,
        String tagLine,
        Integer profileIconId,
        Integer summonerLevel,
        OffsetDateTime linkedAt,
        OffsetDateTime lastSyncedAt
) {}