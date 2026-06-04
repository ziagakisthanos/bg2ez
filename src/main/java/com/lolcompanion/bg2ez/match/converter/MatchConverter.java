package com.lolcompanion.bg2ez.match.converter;

import com.lolcompanion.bg2ez.match.entity.MatchParticipant;
import com.lolcompanion.bg2ez.match.entity.MatchSummary;
import com.lolcompanion.bg2ez.match.model.MatchParticipantModel;
import com.lolcompanion.bg2ez.match.model.MatchSummaryModel;
import org.springframework.stereotype.Component;

@Component
public class MatchConverter {

    public MatchSummaryModel toModel(MatchSummary summary, MatchParticipant participant) {
        return new MatchSummaryModel(
                summary.getMatchId(),
                summary.getGameMode(),
                summary.getGameDuration(),
                summary.getGameStart(),
                summary.getQueueId(),
                queueLabel(summary.getQueueId()),
                toParticipantModel(participant)
        );
    }

    public MatchParticipantModel toParticipantModel(MatchParticipant p) {
        return new MatchParticipantModel(
                p.getChampionName(),
                p.getKills(),
                p.getDeaths(),
                p.getAssists(),
                p.getWin(),
                p.getTotalDamage(),
                p.getGoldEarned(),
                p.getCs(),
                p.getVisionScore(),
                p.getRole(),
                p.getKillParticipation()
        );
    }

    private String queueLabel(Integer queueId) {
        if (queueId == null) return "Unknown";
        return switch (queueId) {
            case 420 -> "Ranked Solo";
            case 440 -> "Ranked Flex";
            case 400 -> "Normal Draft";
            case 450 -> "ARAM";
            case 490 -> "Normal Blind";
            case 700 -> "Clash";
            default -> "Other";
        };
    }
}