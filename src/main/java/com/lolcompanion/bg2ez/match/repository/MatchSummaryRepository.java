package com.lolcompanion.bg2ez.match.repository;

import com.lolcompanion.bg2ez.match.entity.MatchParticipant;
import com.lolcompanion.bg2ez.match.entity.MatchSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface MatchSummaryRepository extends JpaRepository<MatchSummary, String> {
    @Query("SELECT m.matchId FROM MatchSummary m WHERE m.gameStart >= :since AND m.queueId IN (420, 440)")
    List<String> findRankedMatchIdsSince(@Param("since") OffsetDateTime since);

}