package com.lolcompanion.bg2ez.ranked.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(schema = "ranked", name = "entry")
public class RankedEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String puuid;

    @Column(nullable = false)
    private String queueType;

    private String tier;
    private String rank;
    private Integer leaguePoints;
    private Integer wins;
    private Integer losses;
    private OffsetDateTime recordedAt;
}