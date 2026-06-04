package com.lolcompanion.bg2ez.match.repository;

import com.lolcompanion.bg2ez.match.entity.MatchParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MatchParticipantRepository extends JpaRepository<MatchParticipant, UUID> {
    List<MatchParticipant> findByPuuidAndMatchIdIn(String puuid, List<String> matchIds);
    List<MatchParticipant> findByPuuid(String puuid);

}