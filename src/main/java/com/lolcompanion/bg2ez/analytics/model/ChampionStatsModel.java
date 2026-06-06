package com.lolcompanion.bg2ez.analytics.model;

import java.math.BigDecimal;

public record ChampionStatsModel(
        String championName,
        Integer gamesPlayed,
        Integer wins,
        Integer losses,
        BigDecimal winRate,
        BigDecimal avgKda,
        BigDecimal avgCs,
        Integer avgDamage,
        BigDecimal avgVision,
        BigDecimal roamKills,
        BigDecimal earlyKills,
        BigDecimal objParticipation,
        String deathZone,
        BigDecimal wardsPlaced,
        BigDecimal wardsDestroyed,
        BigDecimal comebackKills,
        BigDecimal splitPushKills
) {}