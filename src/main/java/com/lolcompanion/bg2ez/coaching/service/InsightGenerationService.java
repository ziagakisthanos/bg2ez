package com.lolcompanion.bg2ez.coaching.service;

import com.lolcompanion.bg2ez.account.entity.Summoner;
import com.lolcompanion.bg2ez.account.repository.SummonerRepository;
import com.lolcompanion.bg2ez.coaching.converter.InsightConverter;
import com.lolcompanion.bg2ez.coaching.entity.Insight;
import com.lolcompanion.bg2ez.coaching.entity.InsightSection;
import com.lolcompanion.bg2ez.coaching.model.AnalyseRequest;
import com.lolcompanion.bg2ez.coaching.model.InsightModel;
import com.lolcompanion.bg2ez.coaching.model.InsightSectionModel;
import com.lolcompanion.bg2ez.coaching.repository.InsightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InsightGenerationService {

    private final ChatClient chatClient;
    private final ContextBuilder contextBuilder;
    private final InsightRepository insightRepository;
    private final SummonerRepository summonerRepository;
    private final InsightConverter insightConverter;

    @Transactional
    public InsightModel analyse(AnalyseRequest request) {
        Summoner summoner = summonerRepository.findAll()
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No account linked"));

        int window = request.window() != null ? request.window() : defaultWindow(request.type());
        String systemPrompt = loadPrompt(request.type());
        String userContext = buildContext(summoner, request, window);

        String response = chatClient.prompt()
                .system(systemPrompt)
                .user(userContext)
                .call()
                .content();

        List<InsightSection> sections = parseSections(response);

        Insight insight = new Insight();
        insight.setPuuid(summoner.getPuuid());
        insight.setInsightType(request.type());
        insight.setSubject(request.subject());
        insight.setMatchWindow(window);
        insight.setGeneratedAt(OffsetDateTime.now());
        insight.setModelUsed("llama3.1");

        for (InsightSection section : sections) {
            section.setInsight(insight);
        }
        insight.setSections(sections);

        Insight saved = insightRepository.save(insight);
        return insightConverter.entityToModel(saved);
    }

    public List<InsightModel> getInsights() {
        String puuid = summonerRepository.findAll()
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No account linked"))
                .getPuuid();
        return insightRepository.findByPuuidOrderByGeneratedAtDesc(puuid)
                .stream().map(insightConverter::entityToModel).toList();
    }

    public InsightModel getInsight(UUID id) {
        return insightRepository.findById(id)
                .map(insightConverter::entityToModel)
                .orElseThrow(() -> new RuntimeException("Insight not found"));
    }

    public InsightModel getLatestByType(String type) {
        String puuid = summonerRepository.findAll()
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No account linked"))
                .getPuuid();
        return insightRepository
                .findByPuuidAndInsightTypeOrderByGeneratedAtDesc(puuid, type)
                .stream().findFirst()
                .map(insightConverter::entityToModel)
                .orElseThrow(() -> new RuntimeException("No insight found for type: " + type));
    }

    private String buildContext(Summoner summoner, AnalyseRequest request, int window) {
        return switch (request.type()) {
            case "last_game" -> contextBuilder.buildLastGameContext(summoner);
            case "champion" -> contextBuilder.buildChampionContext(
                    summoner, request.subject(), window);
            default -> contextBuilder.buildSessionContext(summoner, window);
        };
    }

    private int defaultWindow(String type) {
        return switch (type) {
            case "last_game" -> 1;
            case "champion" -> 20;
            default -> 10;
        };
    }

    private String loadPrompt(String type) {
        String filename = "champion".equals(type)
                ? "prompts/system-champion.txt"
                : "prompts/system-coaching.txt";
        try {
            return new ClassPathResource(filename)
                    .getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load prompt template: " + filename);
        }
    }

    private List<InsightSection> parseSections(String response) {
        List<InsightSection> sections = new ArrayList<>();
        String[] sectionTypes = {"STRENGTHS", "WEAKNESSES", "PATTERNS", "ADVICE"};

        for (int i = 0; i < sectionTypes.length; i++) {
            String type = sectionTypes[i];
            String next = i + 1 < sectionTypes.length ? sectionTypes[i + 1] : null;

            int start = response.indexOf(type + ":");
            if (start == -1) continue;
            start += type.length() + 1;

            int end = next != null ? response.indexOf(next + ":") : response.length();
            if (end == -1) end = response.length();

            String content = response.substring(start, end).trim();

            InsightSection section = new InsightSection();
            section.setSectionType(type.toLowerCase());
            section.setContent(content);
            section.setDisplayOrder(i);
            sections.add(section);
        }

        if (sections.isEmpty()) {
            InsightSection fallback = new InsightSection();
            fallback.setSectionType("summary");
            fallback.setContent(response);
            fallback.setDisplayOrder(0);
            sections.add(fallback);
        }

        return sections;
    }
}