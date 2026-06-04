package com.lolcompanion.bg2ez.match.model;

import java.math.BigDecimal;

public record MatchParticipantModel(
        String championName,
        Integer kills,
        Integer deaths,
        Integer assists,
        Boolean win,
        Integer totalDamage,
        Integer goldEarned,
        Integer cs,
        Integer visionScore,
        String role,
        BigDecimal killParticipation
) {}