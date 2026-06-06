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

    @Query("SELECT t FROM TimelineEvent t WHERE t.killerPuuid = :puuid AND t.matchId IN :matchIds AND t.eventType = 'CHAMPION_KILL'")
    List<TimelineEvent> findKillsByPuuidAndMatches(
            @Param("puuid") String puuid,
            @Param("matchIds") List<String> matchIds);

    @Query("SELECT t FROM TimelineEvent t WHERE t.victimPuuid = :puuid AND t.matchId IN :matchIds AND t.eventType = 'CHAMPION_KILL'")
    List<TimelineEvent> findDeathsByPuuidAndMatches(
            @Param("puuid") String puuid,
            @Param("matchIds") List<String> matchIds);

    @Query("SELECT t FROM TimelineEvent t WHERE t.killerPuuid = :puuid AND t.matchId IN :matchIds AND t.eventType = 'ELITE_MONSTER_KILL'")
    List<TimelineEvent> findObjectiveKillsByPuuidAndMatches(
            @Param("puuid") String puuid,
            @Param("matchIds") List<String> matchIds);

    @Query("SELECT t FROM TimelineEvent t WHERE t.creatorPuuid = :puuid " +
            "AND t.matchId IN :matchIds AND t.eventType = 'WARD_PLACED'")
    List<TimelineEvent> findWardsPlacedByPuuidAndMatches(
            @Param("puuid") String puuid,
            @Param("matchIds") List<String> matchIds);

    @Query("SELECT t FROM TimelineEvent t WHERE t.killerPuuid = :puuid " +
            "AND t.matchId IN :matchIds AND t.eventType = 'WARD_KILL'")
    List<TimelineEvent> findWardsDestroyedByPuuidAndMatches(
            @Param("puuid") String puuid,
            @Param("matchIds") List<String> matchIds);

    boolean existsByMatchId(String matchId);
}