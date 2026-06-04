package com.lolcompanion.bg2ez.coaching.service;

import com.lolcompanion.bg2ez.account.entity.Summoner;
import com.lolcompanion.bg2ez.match.entity.MatchParticipant;
import com.lolcompanion.bg2ez.match.entity.MatchSummary;
import com.lolcompanion.bg2ez.match.repository.MatchParticipantRepository;
import com.lolcompanion.bg2ez.match.repository.MatchSummaryRepository;
import com.lolcompanion.bg2ez.ranked.entity.RankedEntry;
import com.lolcompanion.bg2ez.ranked.repository.RankedEntryRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ContextBuilder {

    private final MatchSummaryRepository matchSummaryRepository;
    private final MatchParticipantRepository matchParticipantRepository;
    private final RankedEntryRepository rankedEntryRepository;

    public ContextBuilder(MatchSummaryRepository matchSummaryRepository,
                          MatchParticipantRepository matchParticipantRepository,
                          RankedEntryRepository rankedEntryRepository) {
        this.matchSummaryRepository = matchSummaryRepository;
        this.matchParticipantRepository = matchParticipantRepository;
        this.rankedEntryRepository = rankedEntryRepository;
    }

    public String buildSessionContext(Summoner summoner, int window) {
        List<MatchParticipant> participants = getRecentParticipants(
                summoner.getPuuid(), window);
        List<RankedEntry> ranked = rankedEntryRepository
                .findByPuuidOrderByRecordedAtDesc(summoner.getPuuid());

        StringBuilder sb = new StringBuilder();

        appendProfile(sb, summoner, ranked);
        appendSessionStats(sb, participants, window);
        appendChampionBreakdown(sb, participants);

        return sb.toString();
    }

    public String buildLastGameContext(Summoner summoner) {
        List<MatchParticipant> recent = getRecentParticipants(
                summoner.getPuuid(), 1);
        if (recent.isEmpty()) return "No recent games found.";

        MatchParticipant last = recent.get(0);
        List<RankedEntry> ranked = rankedEntryRepository
                .findByPuuidOrderByRecordedAtDesc(summoner.getPuuid());

        StringBuilder sb = new StringBuilder();
        appendProfile(sb, summoner, ranked);

        sb.append("\n== Last Game ==\n");
        sb.append("Champion: ").append(last.getChampionName()).append("\n");
        sb.append("Role: ").append(last.getRole()).append("\n");
        sb.append("Result: ").append(last.getWin() ? "WIN" : "LOSS").append("\n");
        sb.append("KDA: ").append(last.getKills()).append("/")
                .append(last.getDeaths()).append("/")
                .append(last.getAssists()).append("\n");
        sb.append("CS: ").append(last.getCs()).append("\n");
        sb.append("Damage: ").append(last.getTotalDamage()).append("\n");
        sb.append("Vision: ").append(last.getVisionScore()).append("\n");
        sb.append("Kill Participation: ").append(last.getKillParticipation()).append("%\n");

        return sb.toString();
    }

    public String buildChampionContext(Summoner summoner, String championName, int window) {
        List<MatchParticipant> all = getRecentParticipants(summoner.getPuuid(), 100);
        List<MatchParticipant> champGames = all.stream()
                .filter(p -> championName.equalsIgnoreCase(p.getChampionName()))
                .limit(window)
                .toList();

        if (champGames.isEmpty()) return "No games found on " + championName;

        List<RankedEntry> ranked = rankedEntryRepository
                .findByPuuidOrderByRecordedAtDesc(summoner.getPuuid());

        StringBuilder sb = new StringBuilder();
        appendProfile(sb, summoner, ranked);

        sb.append("\n== Champion Deep Dive: ").append(championName).append(" ==\n");
        sb.append("Games analysed: ").append(champGames.size()).append("\n");

        long wins = champGames.stream().filter(MatchParticipant::getWin).count();
        sb.append("Win rate: ").append(wins).append("W / ")
                .append(champGames.size() - wins).append("L (")
                .append(String.format("%.0f", (double) wins / champGames.size() * 100))
                .append("%)\n");

        double avgKda = champGames.stream().mapToDouble(p -> {
            double d = p.getDeaths() == 0 ? 1.0 : p.getDeaths();
            return (p.getKills() + p.getAssists()) / d;
        }).average().orElse(0);
        sb.append("Avg KDA: ").append(String.format("%.2f", avgKda)).append("\n");

        double avgCs = champGames.stream()
                .mapToInt(p -> p.getCs() == null ? 0 : p.getCs())
                .average().orElse(0);
        sb.append("Avg CS: ").append(String.format("%.1f", avgCs)).append("\n");

        double avgDmg = champGames.stream()
                .mapToInt(p -> p.getTotalDamage() == null ? 0 : p.getTotalDamage())
                .average().orElse(0);
        sb.append("Avg Damage: ").append(String.format("%.0f", avgDmg)).append("\n");

        double avgVision = champGames.stream()
                .mapToInt(p -> p.getVisionScore() == null ? 0 : p.getVisionScore())
                .average().orElse(0);
        sb.append("Avg Vision: ").append(String.format("%.1f", avgVision)).append("\n");

        String role = champGames.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getRole() == null ? "UNKNOWN" : p.getRole(),
                        Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("UNKNOWN");
        sb.append("Primary role on this champion: ").append(role).append("\n");

        return sb.toString();
    }

    private void appendProfile(StringBuilder sb, Summoner summoner,
                               List<RankedEntry> ranked) {
        sb.append("== Player Profile ==\n");
        sb.append("Game: ").append(summoner.getGameName())
                .append("#").append(summoner.getTagLine()).append("\n");
        sb.append("Level: ").append(summoner.getSummonerLevel()).append("\n");

        ranked.stream()
                .filter(r -> "RANKED_SOLO_5x5".equals(r.getQueueType()))
                .findFirst()
                .ifPresent(r -> sb.append("Solo Rank: ")
                        .append(r.getTier()).append(" ")
                        .append(r.getRank()).append(" ")
                        .append(r.getLeaguePoints()).append(" LP (")
                        .append(r.getWins()).append("W/")
                        .append(r.getLosses()).append("L)\n"));
    }

    private void appendSessionStats(StringBuilder sb,
                                    List<MatchParticipant> participants, int window) {
        if (participants.isEmpty()) return;

        sb.append("\n== Session Stats (last ").append(window).append(" games) ==\n");
        long wins = participants.stream().filter(MatchParticipant::getWin).count();
        sb.append("Record: ").append(wins).append("W/")
                .append(participants.size() - wins).append("L\n");

        double avgKda = participants.stream().mapToDouble(p -> {
            double d = p.getDeaths() == 0 ? 1.0 : p.getDeaths();
            return (p.getKills() + p.getAssists()) / d;
        }).average().orElse(0);
        sb.append("Avg KDA: ").append(String.format("%.2f", avgKda)).append("\n");

        double avgCs = participants.stream()
                .mapToInt(p -> p.getCs() == null ? 0 : p.getCs())
                .average().orElse(0);
        sb.append("Avg CS: ").append(String.format("%.1f", avgCs)).append("\n");

        double avgVision = participants.stream()
                .mapToInt(p -> p.getVisionScore() == null ? 0 : p.getVisionScore())
                .average().orElse(0);
        sb.append("Avg Vision Score: ").append(String.format("%.1f", avgVision)).append("\n");
    }

    private void appendChampionBreakdown(StringBuilder sb,
                                         List<MatchParticipant> participants) {
        if (participants.isEmpty()) return;

        sb.append("\n== Champion Breakdown ==\n");
        Map<String, List<MatchParticipant>> byChamp = participants.stream()
                .collect(Collectors.groupingBy(MatchParticipant::getChampionName));

        byChamp.forEach((champ, games) -> {
            long wins = games.stream().filter(MatchParticipant::getWin).count();
            double kda = games.stream().mapToDouble(p -> {
                double d = p.getDeaths() == 0 ? 1.0 : p.getDeaths();
                return (p.getKills() + p.getAssists()) / d;
            }).average().orElse(0);
            sb.append(champ).append(": ")
                    .append(games.size()).append("G, ")
                    .append(wins).append("W, KDA ")
                    .append(String.format("%.2f", kda)).append("\n");
        });
    }

    private List<MatchParticipant> getRecentParticipants(String puuid, int limit) {
        return matchParticipantRepository.findByPuuid(puuid)
                .stream()
                .limit(limit)
                .toList();
    }
}