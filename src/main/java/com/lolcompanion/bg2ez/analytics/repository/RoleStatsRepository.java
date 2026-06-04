package com.lolcompanion.bg2ez.analytics.repository;

import com.lolcompanion.bg2ez.analytics.entity.RoleStats;
import com.lolcompanion.bg2ez.analytics.entity.RoleStatsId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoleStatsRepository extends JpaRepository<RoleStats, RoleStatsId> {
    List<RoleStats> findByIdPuuid(String puuid);
}