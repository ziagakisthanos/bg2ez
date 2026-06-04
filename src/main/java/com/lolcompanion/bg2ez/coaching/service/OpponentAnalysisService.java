package com.lolcompanion.bg2ez.coaching.service;

import com.lolcompanion.bg2ez.coaching.entity.OpponentInsight;
import com.lolcompanion.bg2ez.coaching.model.OpponentInsightModel;
import com.lolcompanion.bg2ez.coaching.repository.OpponentInsightRepository;
import com.lolcompanion.bg2ez.riot.client.RiotApiClient;
import com.lolcompanion.bg2ez.riot.model.MatchDetailDto;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OpponentAnalysisService {

    private final ChatClient chatClient;
    private final ContextBuilder contextBuilder;
    private final OpponentInsightRepository opponentInsightRepository;
    private final RiotApiClient riotApiClient;

    public OpponentAnalysisService(ChatClient.Builder chatClientBuilder,
                                   ContextBuilder contextBuilder,
                                   OpponentInsightRepository opponentInsightRepository,
                                   RiotApiClient riotApiClient) {
        this.chatClient = chatClientBuilder.build();
        this.contextBuilder = contextBuilder;
        this.opponentInsightRepository = opponentInsightRepository;
        this.riotApiClient = riotApiClient;
    }

    public List<OpponentInsightModel> analyseEnemies(String gameId,
                                                     List<OpponentRequest> enemies) {
        List<OpponentInsightModel> results = new ArrayList<>();

        for (OpponentRequest enemy : enemies) {
            // Check cache
            OpponentInsight cached = opponentInsightRepository
                    .findByPuuidAndGameId(enemy.puuid(), gameId)
                    .orElse(null);

            if (cached != null) {
                results.add(toModel(cached));
                continue;
            }

            try {
                OpponentInsightModel insight = fetchAndAnalyse(
                        gameId, enemy.puuid(), enemy.championName());
                results.add(insight);

                // Throttle between players
                Thread.sleep(1500);
            } catch (Exception e) {
                results.add(new OpponentInsightModel(
                        enemy.puuid(),
                        enemy.championName(),
                        "Could not fetch data for this player.",
                        null));
            }
        }

        return results;
    }

    private OpponentInsightModel fetchAndAnalyse(String gameId,
                                                 String puuid,
                                                 String championName) throws Exception {
        // Fetch last 10 match IDs
        List<String> matchIds = riotApiClient.getMatchIds(puuid, 10, 0);

        // Fetch match details
        List<MatchDetailDto> matches = new ArrayList<>();
        for (String matchId : matchIds) {
            try {
                matches.add(riotApiClient.getMatchDetail(matchId));
                Thread.sleep(600); // respect rate limit
            } catch (Exception e) {
                // skip failed match
            }
        }

        // Build context and call LLM
        String context = contextBuilder.buildOpponentContext(puuid, championName, matches);
        String systemPrompt = new ClassPathResource("prompts/system-opponent.txt")
                .getContentAsString(StandardCharsets.UTF_8);

        String response = chatClient.prompt()
                .system(systemPrompt)
                .user(context)
                .call()
                .content();

        // Persist
        OpponentInsight insight = new OpponentInsight();
        insight.setPuuid(puuid);
        insight.setGameId(gameId);
        insight.setChampionName(championName);
        insight.setGeneratedAt(OffsetDateTime.now());
        insight.setContent(response);
        opponentInsightRepository.save(insight);

        return toModel(insight);
    }

    private OpponentInsightModel toModel(OpponentInsight entity) {
        return new OpponentInsightModel(
                entity.getPuuid(),
                entity.getChampionName(),
                entity.getContent(),
                entity.getGeneratedAt()
        );
    }

    public record OpponentRequest(String puuid, String championName) {}
}