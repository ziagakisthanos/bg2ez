package com.lolcompanion.bg2ez.match.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Table(schema = "match", name = "participant")
public class MatchParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String matchId;

    @Column(nullable = false)
    private String puuid;

    private Integer championId;
    private String championName;
    private Integer kills;
    private Integer deaths;
    private Integer assists;
    private Boolean win;
    private Integer totalDamage;
    private Integer goldEarned;
    private Integer cs;
    private Integer visionScore;
    private String role;
    private String lane;
    private BigDecimal killParticipation;
    private Integer soloKills;
    private Integer turretDamage;
}