package com.lolcompanion.bg2ez.coaching.repository;

import com.lolcompanion.bg2ez.coaching.entity.OpponentInsight;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OpponentInsightRepository extends JpaRepository<OpponentInsight, UUID> {
    Optional<OpponentInsight> findByPuuidAndGameId(String puuid, String gameId);
    List<OpponentInsight> findByGameId(String gameId);
}