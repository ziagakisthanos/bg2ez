package com.lolcompanion.bg2ez.coaching.model;

import java.time.OffsetDateTime;

public record OpponentInsightModel(
        String puuid,
        String championName,
        String content,
        OffsetDateTime generatedAt
) {}