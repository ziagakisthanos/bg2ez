package com.lolcompanion.bg2ez.account.service;

import com.lolcompanion.bg2ez.account.entity.Summoner;
import com.lolcompanion.bg2ez.account.repository.SummonerRepository;
import com.lolcompanion.bg2ez.riot.client.RiotApiClient;
import com.lolcompanion.bg2ez.riot.model.AccountDto;
import com.lolcompanion.bg2ez.riot.model.SummonerDto;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class AccountService {

    private final RiotApiClient riotApiClient;
    private final SummonerRepository summonerRepository;

    public AccountService(RiotApiClient riotApiClient,
                          SummonerRepository summonerRepository) {
        this.riotApiClient = riotApiClient;
        this.summonerRepository = summonerRepository;
    }

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
}