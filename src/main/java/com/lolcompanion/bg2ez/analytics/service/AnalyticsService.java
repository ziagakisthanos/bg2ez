package com.lolcompanion.bg2ez.analytics.service;

import com.lolcompanion.bg2ez.analytics.entity.*;
import com.lolcompanion.bg2ez.analytics.repository.*;
import com.lolcompanion.bg2ez.config.repository.AppConfigRepository;
import com.lolcompanion.bg2ez.match.entity.MatchParticipant;
import com.lolcompanion.bg2ez.match.repository.MatchParticipantRepository;
import com.lolcompanion.bg2ez.match.repository.MatchSummaryRepository;
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
public class AnalyticsService {

    private final MatchParticipantRepository participantRepository;
    private final MatchSummaryRepository summaryRepository;
    private final ChampionStatsRepository championStatsRepository;
    private final RoleStatsRepository roleStatsRepository;
    private final AppConfigRepository appConfigRepository;

    public AnalyticsService(MatchParticipantRepository participantRepository,
                            MatchSummaryRepository summaryRepository,
                            ChampionStatsRepository championStatsRepository,
                            RoleStatsRepository roleStatsRepository,
                            AppConfigRepository appConfigRepository) {
        this.participantRepository = participantRepository;
        this.summaryRepository = summaryRepository;
        this.championStatsRepository = championStatsRepository;
        this.roleStatsRepository = roleStatsRepository;
        this.appConfigRepository = appConfigRepository;
    }

    @Transactional
    public void computeForPlayer(String puuid) {
        // Get split start timestamp
        long splitStart = appConfigRepository.findById("current_split_start")
                .map(c -> Long.parseLong(c.getValue()))
                .orElse(0L);

        OffsetDateTime splitStartDate = OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(splitStart), ZoneOffset.UTC);

        // Get all match IDs within current split for ranked queues
        List<String> rankedMatchIds = summaryRepository.findRankedMatchIdsSince(splitStartDate);

        // Get participant rows for this player in those matches
        List<MatchParticipant> participants = participantRepository.findByPuuidAndMatchIdIn(puuid, rankedMatchIds);

        if (participants.isEmpty()) return;

        computeChampionStats(puuid, participants);
        computeRoleStats(puuid, participants);
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
}