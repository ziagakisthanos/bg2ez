package com.lolcompanion.bg2ez.coaching.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(schema = "coaching", name = "insight")
public class Insight {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String puuid;

    @Column(nullable = false)
    private String insightType;

    private String subject;
    private Integer matchWindow;
    private OffsetDateTime generatedAt;
    private String modelUsed;
    private Integer promptTokens;
    private Integer completionTokens;

    @OneToMany(mappedBy = "insight", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<InsightSection> sections;
}