package com.lolcompanion.bg2ez.ranked.repository;

import com.lolcompanion.bg2ez.ranked.entity.RankedEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface RankedEntryRepository extends JpaRepository<RankedEntry, UUID> {
    List<RankedEntry> findByPuuidOrderByRecordedAtDesc(String puuid);
    List<RankedEntry> findByPuuidAndQueueTypeOrderByRecordedAtDesc(
            String puuid, String queueType);
}