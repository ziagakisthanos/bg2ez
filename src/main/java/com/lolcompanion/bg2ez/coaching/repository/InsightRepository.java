package com.lolcompanion.bg2ez.coaching.repository;

import com.lolcompanion.bg2ez.coaching.entity.Insight;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface InsightRepository extends JpaRepository<Insight, UUID> {
    List<Insight> findByPuuidOrderByGeneratedAtDesc(String puuid);
    List<Insight> findByPuuidAndInsightTypeOrderByGeneratedAtDesc(
            String puuid, String insightType);
}