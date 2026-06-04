package com.lolcompanion.bg2ez.account.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(schema = "account", name = "summoner")
public class Summoner {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String puuid;

    @Column(nullable = false)
    private String gameName;

    @Column(nullable = false)
    private String tagLine;

    private Integer profileIconId;
    private Integer summonerLevel;
    private OffsetDateTime linkedAt;
    private OffsetDateTime lastSyncedAt;
}