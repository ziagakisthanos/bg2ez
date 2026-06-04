package com.lolcompanion.bg2ez.live.service;

import com.lolcompanion.bg2ez.account.repository.SummonerRepository;
import com.lolcompanion.bg2ez.live.model.LiveGameModel;
import com.lolcompanion.bg2ez.live.model.LivePlayerModel;
import com.lolcompanion.bg2ez.riot.client.RiotApiClient;
import com.lolcompanion.bg2ez.riot.model.LiveGameDto;
import com.lolcompanion.bg2ez.riot.model.RankedEntryDto;
import com.lolcompanion.bg2ez.riot.model.AccountDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LiveGameService {

    private final RiotApiClient riotApiClient;
    private final SummonerRepository summonerRepository;

    public LiveGameService(RiotApiClient riotApiClient,
                           SummonerRepository summonerRepository) {
        this.riotApiClient = riotApiClient;
        this.summonerRepository = summonerRepository;
    }

    public LiveGameModel getLiveGame() {
        String myPuuid = summonerRepository.findAll()
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No account linked"))
                .getPuuid();

        LiveGameDto game = riotApiClient.getLiveGame(myPuuid);

        // Find my team
        Long myTeamId = game.participants().stream()
                .filter(p -> myPuuid.equals(p.puuid()))
                .map(LiveGameDto.LiveParticipantDto::teamId)
                .findFirst()
                .orElse(100L);

        List<LiveGameDto.LiveParticipantDto> allies = game.participants().stream()
                .filter(p -> p.teamId().equals(myTeamId))
                .toList();

        List<LiveGameDto.LiveParticipantDto> enemies = game.participants().stream()
                .filter(p -> !p.teamId().equals(myTeamId))
                .toList();

        return new LiveGameModel(
                game.gameId().toString(),
                game.gameMode(),
                queueLabel(game.gameQueueConfigId()),
                game.gameLength(),
                allies.stream().map(p -> toPlayerModel(p, myPuuid)).toList(),
                enemies.stream().map(p -> toPlayerModel(p, myPuuid)).toList()
        );
    }

    private LivePlayerModel toPlayerModel(
            LiveGameDto.LiveParticipantDto participant, String myPuuid) {

        String displayName = participant.riotId();
        if (displayName == null || displayName.isBlank()) {
            try {
                AccountDto account = riotApiClient.getAccountByPuuid(participant.puuid());
                displayName = account.gameName() + "#" + account.tagLine();
            } catch (Exception e) {
                displayName = "Unknown";
            }
        }

        String tier = null;
        String rank = null;
        Integer lp = null;
        Integer wins = null;
        Integer losses = null;
        Double winRate = null;

        try {
            List<RankedEntryDto> ranked =
                    riotApiClient.getRankedEntriesByPuuid(participant.puuid());
            RankedEntryDto solo = ranked.stream()
                    .filter(r -> "RANKED_SOLO_5x5".equals(r.queueType()))
                    .findFirst()
                    .orElse(null);

            if (solo != null) {
                tier = solo.tier();
                rank = solo.rank();
                lp = solo.leaguePoints();
                wins = solo.wins();
                losses = solo.losses();
                if (wins != null && losses != null && wins + losses > 0) {
                    winRate = (double) wins / (wins + losses) * 100;
                    winRate = Math.round(winRate * 100.0) / 100.0;
                }
            }
        } catch (Exception e) {
            // player == unranked, null
        }

        return new LivePlayerModel(
                participant.puuid(),
                displayName,
                participant.championId(),
                tier,
                rank,
                lp,
                wins,
                losses,
                winRate,
                myPuuid.equals(participant.puuid())
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
            default -> "Other";
        };
    }
}