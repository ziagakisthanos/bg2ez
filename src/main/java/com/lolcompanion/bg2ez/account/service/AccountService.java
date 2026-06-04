package com.lolcompanion.bg2ez.account.service;

import com.lolcompanion.bg2ez.account.entity.Summoner;
import com.lolcompanion.bg2ez.account.repository.SummonerRepository;
import com.lolcompanion.bg2ez.analytics.repository.ChampionStatsRepository;
import com.lolcompanion.bg2ez.analytics.repository.RoleStatsRepository;
import com.lolcompanion.bg2ez.coaching.repository.InsightRepository;
import com.lolcompanion.bg2ez.coaching.repository.OpponentInsightRepository;
import com.lolcompanion.bg2ez.match.repository.MatchParticipantRepository;
import com.lolcompanion.bg2ez.match.repository.MatchSummaryRepository;
import com.lolcompanion.bg2ez.ranked.repository.RankedEntryRepository;
import com.lolcompanion.bg2ez.riot.client.RiotApiClient;
import com.lolcompanion.bg2ez.riot.model.AccountDto;
import com.lolcompanion.bg2ez.riot.model.SummonerDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final RiotApiClient riotApiClient;
    private final SummonerRepository summonerRepository;
    private final OpponentInsightRepository opponentInsightRepository;
    private final InsightRepository insightRepository;
    private final RankedEntryRepository rankedEntryRepository;
    private final MatchParticipantRepository matchParticipantRepository;
    private final MatchSummaryRepository matchSummaryRepository;
    private final ChampionStatsRepository championStatsRepository;
    private final RoleStatsRepository roleStatsRepository;


    public Summoner linkAccount(String gameName, String tagLine) {
        AccountDto account = riotApiClient.getAccountByRiotId(gameName, tagLine);
        SummonerDto summoner = riotApiClient.getSummonerByPuuid(account.puuid());

        return summonerRepository.findByPuuid(account.puuid())
                .map(existing -> {
                    existing.setGameName(account.gameName());
                    existing.setTagLine(account.tagLine());
                    existing.setProfileIconId(summoner.profileIconId());
                    existing.setSummonerLevel(summoner.summonerLevel());
                    return summonerRepository.save(existing);
                })
                .orElseGet(() -> {
                    Summoner s = new Summoner();
                    s.setPuuid(account.puuid());
                    s.setGameName(account.gameName());
                    s.setTagLine(account.tagLine());
                    s.setProfileIconId(summoner.profileIconId());
                    s.setSummonerLevel(summoner.summonerLevel());
                    s.setLinkedAt(OffsetDateTime.now());
                    return summonerRepository.save(s);
                });
    }

    public Optional<Summoner> getLinkedAccount() {
        return summonerRepository.findAll()
                .stream()
                .findFirst();
    }

    public void unlinkAccount() {
        summonerRepository.findAll()
                .stream()
                .findFirst()
                .ifPresent(summoner -> {
                    opponentInsightRepository.deleteAll();
                    matchParticipantRepository.deleteAll();
                    matchSummaryRepository.deleteAll();
                    championStatsRepository.deleteAll();
                    roleStatsRepository.deleteAll();
                    summonerRepository.delete(summoner);
                });
    }
}