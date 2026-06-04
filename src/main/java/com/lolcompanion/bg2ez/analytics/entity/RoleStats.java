package com.lolcompanion.bg2ez.analytics.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(schema = "analytics", name = "role_stats")
public class RoleStats {

    @EmbeddedId
    private RoleStatsId id;

    private Integer gamesPlayed;
    private Integer wins;
    private Integer losses;
    private BigDecimal avgKda;
    private OffsetDateTime lastComputed;
}