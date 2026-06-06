package com.lolcompanion.bg2ez.match.repository;

import com.lolcompanion.bg2ez.match.entity.TimelineEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TimelineEventRepository extends JpaRepository<TimelineEvent, UUID> {

    List<TimelineEvent> findByKillerPuuidAndMatchIdIn(String puuid, List<String> matchIds);
    List<TimelineEvent> findByVictimPuuidAndMatchIdIn(String puuid, List<String> matchIds);

    @Query("SELECT t FROM TimelineEvent t WHERE t.matchId IN :matchIds " +
            "AND (t.killerPuuid = :puuid OR t.victimPuuid = :puuid)")
    List<TimelineEvent> findByPuuidInvolvementAndMatchIds(
            @Param("puuid") String puuid,
            @Param("matchIds") List<String> matchIds);

    boolean existsByMatchId(String matchId);
}