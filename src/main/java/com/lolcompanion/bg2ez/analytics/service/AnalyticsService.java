package com.lolcompanion.bg2ez.analytics.service;

import com.lolcompanion.bg2ez.analytics.entity.*;
import com.lolcompanion.bg2ez.analytics.repository.*;
import com.lolcompanion.bg2ez.config.repository.AppConfigRepository;
import com.lolcompanion.bg2ez.match.entity.MatchParticipant;
import com.lolcompanion.bg2ez.match.entity.TimelineEvent;
import com.lolcompanion.bg2ez.riot.utils.MapZoneClassifier;
import com.lolcompanion.bg2ez.match.repository.MatchParticipantRepository;
import com.lolcompanion.bg2ez.match.repository.MatchSummaryRepository;
import com.lolcompanion.bg2ez.match.repository.TimelineEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final MatchParticipantRepository participantRepository;
    private final MatchSummaryRepository summaryRepository;
    private final ChampionStatsRepository championStatsRepository;
    private final RoleStatsRepository roleStatsRepository;
    private final AppConfigRepository appConfigRepository;
    private final TimelineEventRepository timelineEventRepository;
    private final MatchParticipantRepository matchParticipantRepository;
    private final MapZoneClassifier mapZoneClassifier;

    @Transactional
    public void computeForPlayer(String puuid) {
        // Get split start timestamp
        long splitStart = appConfigRepository.findById("current_split_start")
                .map(c -> Long.parseLong(c.getValue()))
                .orElse(0L);

        OffsetDateTime splitStartDate = OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(splitStart), ZoneOffset.UTC);

        List<String> rankedMatchIds = summaryRepository.findRankedMatchIdsSince(splitStartDate);
        List<MatchParticipant> participants = participantRepository.findByPuuidAndMatchIdIn(puuid, rankedMatchIds);
        if (participants.isEmpty()) return;

        computeChampionStats(puuid, participants);
        computeRoleStats(puuid, participants);
        computeTimelineMetrics(puuid, rankedMatchIds, participants);
    }

    private void computeChampionStats(String puuid,
                                      List<MatchParticipant> participants) {
        Map<String, List<MatchParticipant>> byChampion = participants.stream()
                .collect(Collectors.groupingBy(MatchParticipant::getChampionName));

        for (var entry : byChampion.entrySet()) {
            String champion = entry.getKey();
            List<MatchParticipant> games = entry.getValue();

            int played = games.size();
            int wins = (int) games.stream().filter(MatchParticipant::getWin).count();
            double avgKda = games.stream()
                    .mapToDouble(p -> {
                        double deaths = p.getDeaths() == 0 ? 1.0 : p.getDeaths();
                        return (p.getKills() + p.getAssists()) / deaths;
                    })
                    .average().orElse(0);
            double avgCs = games.stream()
                    .mapToInt(p -> p.getCs() == null ? 0 : p.getCs())
                    .average().orElse(0);
            double avgDamage = games.stream()
                    .mapToInt(p -> p.getTotalDamage() == null ? 0 : p.getTotalDamage())
                    .average().orElse(0);
            double avgVision = games.stream()
                    .mapToInt(p -> p.getVisionScore() == null ? 0 : p.getVisionScore())
                    .average().orElse(0);

            ChampionStatsId id = new ChampionStatsId();
            id.setPuuid(puuid);
            id.setChampionName(champion);

            ChampionStats stats = championStatsRepository.findById(id)
                    .orElse(new ChampionStats());
            stats.setId(id);
            stats.setGamesPlayed(played);
            stats.setWins(wins);
            stats.setLosses(played - wins);
            stats.setAvgKda(bd(avgKda));
            stats.setAvgCs(bd(avgCs));
            stats.setAvgDamage((int) avgDamage);
            stats.setAvgVision(bd(avgVision));
            stats.setLastComputed(OffsetDateTime.now());
            championStatsRepository.save(stats);
        }
    }

    private void computeRoleStats(String puuid,
                                  List<MatchParticipant> participants) {
        Map<String, List<MatchParticipant>> byRole = participants.stream()
                .filter(p -> p.getRole() != null && !p.getRole().isBlank())
                .collect(Collectors.groupingBy(MatchParticipant::getRole));

        for (var entry : byRole.entrySet()) {
            String role = entry.getKey();
            List<MatchParticipant> games = entry.getValue();

            int played = games.size();
            int wins = (int) games.stream().filter(MatchParticipant::getWin).count();
            double avgKda = games.stream()
                    .mapToDouble(p -> {
                        double deaths = p.getDeaths() == 0 ? 1.0 : p.getDeaths();
                        return (p.getKills() + p.getAssists()) / deaths;
                    })
                    .average().orElse(0);

            RoleStatsId id = new RoleStatsId();
            id.setPuuid(puuid);
            id.setRole(role);

            RoleStats stats = roleStatsRepository.findById(id)
                    .orElse(new RoleStats());
            stats.setId(id);
            stats.setGamesPlayed(played);
            stats.setWins(wins);
            stats.setLosses(played - wins);
            stats.setAvgKda(bd(avgKda));
            stats.setLastComputed(OffsetDateTime.now());
            roleStatsRepository.save(stats);
        }
    }

    private BigDecimal bd(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    public List<ChampionStats> getChampionStats(String puuid) {
        return championStatsRepository.findByIdPuuidOrderByGamesPlayedDesc(puuid);
    }

    public List<RoleStats> getRoleStats(String puuid) {
        return roleStatsRepository.findByIdPuuid(puuid);
    }

    private void computeTimelineMetrics(String puuid,
                                        List<String> rankedMatchIds,
                                        List<MatchParticipant> participants) {
        if (rankedMatchIds.isEmpty()) return;

        Map<String, List<MatchParticipant>> byChampion = participants.stream()
                .collect(Collectors.groupingBy(MatchParticipant::getChampionName));

        List<TimelineEvent> kills = timelineEventRepository
                .findKillsByPuuidAndMatches(puuid, rankedMatchIds);
        List<TimelineEvent> deaths = timelineEventRepository
                .findDeathsByPuuidAndMatches(puuid, rankedMatchIds);
        List<TimelineEvent> objectives = timelineEventRepository
                .findObjectiveKillsByPuuidAndMatches(puuid, rankedMatchIds);
        List<TimelineEvent> wardsPlacedEvents = timelineEventRepository
                .findWardsPlacedByPuuidAndMatches(puuid, rankedMatchIds);
        List<TimelineEvent> wardsDestroyedEvents = timelineEventRepository
                .findWardsDestroyedByPuuidAndMatches(puuid, rankedMatchIds);

        Map<String, String> matchToChampion = participants.stream()
                .collect(Collectors.toMap(
                        MatchParticipant::getMatchId,
                        MatchParticipant::getChampionName,
                        (a, b) -> a));

        Map<String, String> matchToRole = participants.stream()
                .collect(Collectors.toMap(
                        MatchParticipant::getMatchId,
                        p -> p.getRole() == null ? "UNKNOWN" : p.getRole(),
                        (a, b) -> a));

        for (var entry : byChampion.entrySet()) {
            String champion = entry.getKey();
            List<MatchParticipant> games = entry.getValue();
            List<String> champMatchIds = games.stream()
                    .map(MatchParticipant::getMatchId)
                    .toList();

            int totalGames = games.size();

            List<TimelineEvent> champKills = kills.stream()
                    .filter(k -> champMatchIds.contains(k.getMatchId()))
                    .toList();
            List<TimelineEvent> champDeaths = deaths.stream()
                    .filter(d -> champMatchIds.contains(d.getMatchId()))
                    .toList();
            List<TimelineEvent> champObjectives = objectives.stream()
                    .filter(o -> champMatchIds.contains(o.getMatchId()))
                    .toList();
            List<TimelineEvent> champWardsPlaced = wardsPlacedEvents.stream()
                    .filter(w -> champMatchIds.contains(w.getMatchId()))
                    .toList();
            List<TimelineEvent> champWardsDestroyed = wardsDestroyedEvents.stream()
                    .filter(w -> champMatchIds.contains(w.getMatchId()))
                    .toList();

            String role = matchToRole.getOrDefault(games.getFirst().getMatchId(), "UNKNOWN");
            String expectedZone = mapZoneClassifier.roleToExpectedZone(role);

            // 1. Roam kills — kills in a zone different from expected role zone
            long roamKills = champKills.stream()
                    .filter(k -> k.getMapZone() != null
                            && !k.getMapZone().equals(expectedZone)
                            && !k.getMapZone().equals("JUNGLE")
                            && !mapZoneClassifier.isObjective(k.getMapZone()))
                    .count();

            // 2. Early game kills — before 10 minutes
            long earlyKills = champKills.stream()
                    .filter(k -> k.getTimestampMs() != null && k.getTimestampMs() < 600000)
                    .count();

            // 3. Objective participation — kills/assists near dragon or baron
            long objParticipation = champObjectives.size()
                    + champKills.stream()
                    .filter(k -> mapZoneClassifier.isObjective(k.getMapZone()))
                    .count();

            // 4. Death zone pattern — most common death zone
            String deathZone = champDeaths.stream()
                    .filter(d -> d.getMapZone() != null)
                    .collect(Collectors.groupingBy(TimelineEvent::getMapZone, Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("UNKNOWN");

            // 5. Vision denied and placed per game
            BigDecimal avgWardsPlaced = bd((double) champWardsPlaced.size() / totalGames);
            BigDecimal avgWardsDestroyed = bd((double) champWardsDestroyed.size() / totalGames);

            // 6. Comeback kills — kills after timestamp > 20 min (late game)
            // Simple heuristic: kills after 20min in a losing position
            // We use late game kills (>1200000ms) as a proxy for comeback factor
            long comebackKills = champKills.stream()
                    .filter(k -> k.getTimestampMs() != null && k.getTimestampMs() > 1200000)
                    .count();

            // 7. Split push kills — kills in side lanes near structures
            long splitPushKills = champKills.stream()
                    .filter(k -> k.getMapZone() != null
                            && mapZoneClassifier.isSideLane(k.getMapZone()))
                    .count();

            BigDecimal avgRoam = bd((double) roamKills / totalGames);
            BigDecimal avgEarly = bd((double) earlyKills / totalGames);
            BigDecimal avgObj = bd((double) objParticipation / totalGames);
            BigDecimal avgComeback = bd((double) comebackKills / totalGames);
            BigDecimal avgSplitPush = bd((double) splitPushKills / totalGames);

            ChampionStatsId id = new ChampionStatsId();
            id.setPuuid(puuid);
            id.setChampionName(champion);

            championStatsRepository.findById(id).ifPresent(stats -> {
                stats.setRoamKills(avgRoam);
                stats.setEarlyKills(avgEarly);
                stats.setObjParticipation(avgObj);
                stats.setDeathZone(deathZone);
                stats.setWardsPlaced(avgWardsPlaced);
                stats.setWardsDestroyed(avgWardsDestroyed);
                stats.setComebackKills(avgComeback);
                stats.setSplitPushKills(avgSplitPush);
                championStatsRepository.save(stats);
            });
        }
    }
}