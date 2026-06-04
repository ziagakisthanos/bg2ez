package com.lolcompanion.bg2ez.analytics.repository;

import com.lolcompanion.bg2ez.analytics.entity.ChampionStats;
import com.lolcompanion.bg2ez.analytics.entity.ChampionStatsId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChampionStatsRepository extends JpaRepository<ChampionStats, ChampionStatsId> {
    List<ChampionStats> findByIdPuuidOrderByGamesPlayedDesc(String puuid);
}