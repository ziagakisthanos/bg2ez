package com.lolcompanion.bg2ez.match.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(schema = "match", name = "timeline_event")
public class TimelineEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @Column(name = "match_id", nullable = false)
    private String matchId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "timestamp_ms", nullable = false)
    private Long timestampMs;

    @Column(name = "killer_puuid")
    private String killerPuuid;

    @Column(name = "victim_puuid")
    private String victimPuuid;

    @Column(name = "position_x")
    private Integer positionX;

    @Column(name = "position_y")
    private Integer positionY;

    @Column(name = "map_zone")
    private String mapZone;

    @Column(name = "assisting_puuids", columnDefinition = "TEXT[]")
    @org.hibernate.annotations.Array(length = 10)
    private String[] assistingPuuids;
}