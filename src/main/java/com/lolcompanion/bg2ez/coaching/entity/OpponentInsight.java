package com.lolcompanion.bg2ez.coaching.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(schema = "coaching", name = "opponent_insight")
public class OpponentInsight {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String puuid;

    @Column(nullable = false)
    private String gameId;

    private String championName;
    private OffsetDateTime generatedAt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
}