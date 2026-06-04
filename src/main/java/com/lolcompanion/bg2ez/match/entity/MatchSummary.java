package com.lolcompanion.bg2ez.match.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(schema = "match", name = "summary")
public class MatchSummary {
    @Id
    private String matchId;

    private String gameMode;
    private Integer gameDuration;
    private OffsetDateTime gameStart;
    private Integer queueId;
}
