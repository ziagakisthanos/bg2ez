package com.lolcompanion.bg2ez.analytics.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(schema = "analytics", name = "champion_stats")
public class ChampionStats {

    @EmbeddedId
    private ChampionStatsId id;

    private Integer gamesPlayed;
    private Integer wins;
    private Integer losses;
    private BigDecimal avgKda;
    private BigDecimal avgCs;
    private Integer avgDamage;
    private BigDecimal avgVision;
    private OffsetDateTime lastComputed;
    private BigDecimal roamKills;
    private BigDecimal earlyKills;
    private BigDecimal objParticipation;
    private String deathZone;
    private BigDecimal wardsPlaced;
    private BigDecimal wardsDestroyed;
    private BigDecimal comebackKills;
    private BigDecimal splitPushKills;
}