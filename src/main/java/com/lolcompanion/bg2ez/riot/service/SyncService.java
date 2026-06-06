package com.lolcompanion.bg2ez.riot.service;

import com.lolcompanion.bg2ez.account.repository.SummonerRepository;
import com.lolcompanion.bg2ez.analytics.service.AnalyticsService;
import com.lolcompanion.bg2ez.config.repository.AppConfigRepository;
import com.lolcompanion.bg2ez.match.entity.MatchParticipant;
import com.lolcompanion.bg2ez.match.entity.MatchSummary;
import com.lolcompanion.bg2ez.match.repository.MatchParticipantRepository;
import com.lolcompanion.bg2ez.match.repository.MatchSummaryRepository;
import com.lolcompanion.bg2ez.match.repository.TimelineEventRepository;
import com.lolcompanion.bg2ez.ranked.entity.RankedEntry;
import com.lolcompanion.bg2ez.ranked.repository.RankedEntryRepository;
import com.lolcompanion.bg2ez.riot.client.RiotApiClient;
import com.lolcompanion.bg2ez.riot.model.MatchDetailDto;
import com.lolcompanion.bg2ez.riot.model.RankedEntryDto;
import com.lolcompanion.bg2ez.riot.utils.MapZoneClassifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lolcompanion.bg2ez.match.entity.TimelineEvent;
import com.lolcompanion.bg2ez.riot.model.MatchTimelineDto;
import java.util.Map;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SyncService {

    private final RiotApiClient riotApiClient;
    private final SummonerRepository summonerRepository;
    private final MatchSummaryRepository matchSummaryRepository;
    private final MatchParticipantRepository matchParticipantRepository;
    private final AppConfigRepository appConfigRepository;
    private final RankedEntryRepository rankedEntryRepository;
    private final AnalyticsService analyticsService;
    private final TimelineEventRepository timelineEventRepository;
    private final MapZoneClassifier mapZoneClassifier;


    @Transactional
    public SyncResult sync(int count) {
        // Get linked account
        var summoner = summonerRepository.findAll()
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No account linked"));

        // Get current split start timestamp from config
        long splitStart = appConfigRepository.findById("current_split_start")
                .map(c -> Long.parseLong(c.getValue()))
                .orElse(0L);

        List<String> matchIds = riotApiClient.getMatchIds(
                summoner.getPuuid(), count, splitStart);

        // Diff IDs only not already in DB
        List<String> newIds = matchIds.stream()
                .filter(id -> !matchSummaryRepository.existsById(id))
                .toList();

        int fetched = 0;
        for (String matchId : newIds) {
            try {
                MatchDetailDto detail = riotApiClient.getMatchDetail(matchId);
                persist(detail);
                // timeline
                try {
                    MatchTimelineDto timeline = riotApiClient.getMatchTimeline(matchId);
                    persistTimeline(detail, timeline);
                } catch (Exception e) {
                    System.err.println("Failed to fetch timeline for " + matchId + ": " + e.getMessage());
                }
                fetched++;

                // Throttle only for large batches
                if (count > 20) {
                    Thread.sleep(1200);
                }
            } catch (Exception e) {
                // Log and continue — don't fail entire sync for one bad match
                System.err.println("Failed to fetch match " + matchId + ": " + e.getMessage());
            }
        }

        syncRanked(summoner.getPuuid());
        summoner.setLastSyncedAt(OffsetDateTime.now());
        summonerRepository.save(summoner);
        analyticsService.computeForPlayer(summoner.getPuuid());

        return new SyncResult(matchIds.size(), newIds.size(), fetched);
    }

    private void persist(MatchDetailDto detail) throws Exception {
        var info = detail.info();
        var meta = detail.metadata();

        MatchSummary summary = new MatchSummary();
        summary.setMatchId(detail.metadata().matchId());
        summary.setGameMode(info.gameMode());
        summary.setGameDuration(info.gameDuration() != null
                ? info.gameDuration().intValue() : null);
        summary.setGameStart(info.gameStartTimestamp() != null
                ? OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(info.gameStartTimestamp()), ZoneOffset.UTC)
                : null);
        summary.setQueueId(info.queueId());
        matchSummaryRepository.save(summary);

        for (var p : info.participants()) {
            MatchParticipant participant = new MatchParticipant();
            participant.setMatchId(meta.matchId());
            participant.setPuuid(p.puuid());
            participant.setChampionId(p.championId());
            participant.setChampionName(p.championName());
            participant.setKills(p.kills());
            participant.setDeaths(p.deaths());
            participant.setAssists(p.assists());
            participant.setWin(p.win());
            participant.setTotalDamage(p.totalDamageDealtToChampions());
            participant.setGoldEarned(p.goldEarned());
            participant.setCs(p.totalMinionsKilled());
            participant.setVisionScore(p.visionScore());
            participant.setRole(p.teamPosition());
            participant.setLane(p.lane());
            participant.setTurretDamage(p.turretKills());

            // Compute kill participation
            int teamKills = info.participants().stream()
                    .filter(tp -> tp.win().equals(p.win()))
                    .mapToInt(MatchDetailDto.ParticipantDto::kills)
                    .sum();
            if (teamKills > 0) {
                double kp = (double)(p.kills() + p.assists()) / teamKills * 100;
                participant.setKillParticipation(
                        BigDecimal.valueOf(Math.round(kp * 100.0) / 100.0));
            }

            matchParticipantRepository.save(participant);
        }
    }

    private void syncRanked(String puuid) {
        List<RankedEntryDto> entries = riotApiClient.getRankedEntriesByPuuid(puuid);
        for (RankedEntryDto dto : entries) {
            RankedEntry entry = new RankedEntry();
            entry.setPuuid(puuid);
            entry.setQueueType(dto.queueType());
            entry.setTier(dto.tier());
            entry.setRank(dto.rank());
            entry.setLeaguePoints(dto.leaguePoints());
            entry.setWins(dto.wins());
            entry.setLosses(dto.losses());
            entry.setRecordedAt(OffsetDateTime.now());
            rankedEntryRepository.save(entry);
        }
    }

    public record SyncResult(int found, int newMatches, int fetched) {}

    //TODO: check time complexity here, make it run async IF POSSIBLE
    private void persistTimeline(MatchDetailDto detail, MatchTimelineDto timeline) {
        // Build participantId -> puuid map from match detail
        Map<Integer, String> participantMap = detail.info().participants().stream()
                .collect(Collectors.toMap(
                        MatchDetailDto.ParticipantDto::participantId,
                        MatchDetailDto.ParticipantDto::puuid));

        String matchId = detail.metadata().matchId();

        for (MatchTimelineDto.FrameDto frame : timeline.info().frames()) {
            for (MatchTimelineDto.EventDto event : frame.events()) {
                if (!isRelevantEvent(event.type())) continue;

                // parse string to int because riot gems
                String killerPuuid = event.killerId() != null
                        ? participantMap.get(Integer.parseInt(event.killerId())) : null;
                String victimPuuid = event.victimId() != null
                        ? participantMap.get(Integer.parseInt(event.victimId())) : null;
                String creatorPuuid = event.creatorId() != null
                        ? participantMap.get(Integer.parseInt(event.creatorId())) : null;

                String zone = null;
                if (event.position() != null) {
                    zone = mapZoneClassifier.classify(
                            event.position().x(), event.position().y());
                }

                String[] assistPuuids = null;
                if (event.assistingParticipantIds() != null) {
                    assistPuuids = event.assistingParticipantIds().stream()
                            .map(id -> participantMap.get(Integer.parseInt(id)))
                            .filter(Objects::nonNull)
                            .toArray(String[]::new);
                }

                TimelineEvent te = new TimelineEvent();
                te.setMatchId(matchId);
                te.setEventType(event.type());
                te.setTimestampMs(event.timestamp());
                te.setKillerPuuid(killerPuuid);
                te.setVictimPuuid(victimPuuid);
                te.setCreatorPuuid(creatorPuuid);
                te.setWardType(event.wardType());
                te.setPositionX(event.position() != null ? event.position().x() : null);
                te.setPositionY(event.position() != null ? event.position().y() : null);
                te.setMapZone(zone);
                te.setAssistingPuuids(assistPuuids);
                timelineEventRepository.save(te);
            }
        }
    }

    private boolean isRelevantEvent(String type) {
        return switch (type) {
            case "CHAMPION_KILL",
                 "WARD_PLACED",
                 "WARD_KILL",
                 "ELITE_MONSTER_KILL",
                 "BUILDING_KILL" -> true;
            default -> false;
        };
    }
}