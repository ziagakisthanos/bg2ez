package com.lolcompanion.bg2ez.coaching.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(schema = "coaching", name = "insight_section")
public class InsightSection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insight_id", nullable = false)
    private Insight insight;

    @Column(nullable = false)
    private String sectionType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private Integer displayOrder;
}