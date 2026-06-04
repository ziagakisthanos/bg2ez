package com.lolcompanion.bg2ez.analytics.model;

import java.math.BigDecimal;

public record RoleStatsModel(
        String role,
        Integer gamesPlayed,
        Integer wins,
        Integer losses,
        BigDecimal winRate,
        BigDecimal avgKda
) {}