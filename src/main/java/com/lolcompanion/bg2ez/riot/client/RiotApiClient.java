package com.lolcompanion.bg2ez.riot.client;

import com.lolcompanion.bg2ez.riot.config.RiotProperties;
import com.lolcompanion.bg2ez.riot.model.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class RiotApiClient {

    private final RestClient regionalClient;
    private final RestClient platformClient;
    private final RiotProperties props;


    public RiotApiClient(RiotProperties props) {
        this.props = props;
        this.regionalClient = RestClient.builder()
                .baseUrl("https://europe.api.riotgames.com")
                .build();
        this.platformClient = RestClient.builder()
                .baseUrl("https://eun1.api.riotgames.com")
                .build();

    }

    public AccountDto getAccountByRiotId(String gameName, String tagLine) {
        return regionalClient.get()
                .uri("/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}", gameName, tagLine)
                .header("X-Riot-Token", props.apiKey())
                .retrieve()
                .body(AccountDto.class);
    }

    public SummonerDto getSummonerByPuuid(String puuid) {

        return platformClient.get()
                .uri("/lol/summoner/v4/summoners/by-puuid/{puuid}", puuid)
                .header("X-Riot-Token", props.apiKey())
                .retrieve()
                .body(SummonerDto.class);
    }

    public List<String> getMatchIds(String puuid, int count, long startTime) {
        return regionalClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/lol/match/v5/matches/by-puuid/{puuid}/ids")
                        .queryParam("start", 0)
                        .queryParam("count", count)
                        .queryParam("startTime", startTime)
                        .build(puuid))
                .header("X-Riot-Token", props.apiKey())
                .retrieve()
                .body(new ParameterizedTypeReference<List<String>>() {});
    }

    public MatchDetailDto getMatchDetail(String matchId) {
        return regionalClient.get()
                .uri("/lol/match/v5/matches/{matchId}", matchId)
                .header("X-Riot-Token", props.apiKey())
                .retrieve()
                .body(MatchDetailDto.class);
    }



    public List<RankedEntryDto> getRankedEntriesByPuuid(String puuid) {
        return platformClient.get()
                .uri("/lol/league/v4/entries/by-puuid/{puuid}", puuid)
                .header("X-Riot-Token", props.apiKey())
                .retrieve()
                .body(new ParameterizedTypeReference<List<RankedEntryDto>>() {});
    }

    public LiveGameDto getLiveGame(String puuid) {
        return platformClient.get()
                .uri("/lol/spectator/v5/active-games/by-summoner/{puuid}", puuid)
                .header("X-Riot-Token", props.apiKey())
                .retrieve()
                .body(LiveGameDto.class);
    }

    public AccountDto getAccountByPuuid(String puuid) {
        return regionalClient.get()
                .uri("/riot/account/v1/accounts/by-puuid/{puuid}", puuid)
                .header("X-Riot-Token", props.apiKey())
                .retrieve()
                .body(AccountDto.class);
    }

    public MatchTimelineDto getMatchTimeline(String matchId) {
        return regionalClient.get()
                .uri("/lol/match/v5/matches/{matchId}/timeline", matchId)
                .header("X-Riot-Token", props.apiKey())
                .retrieve()
                .body(MatchTimelineDto.class);
    }
}