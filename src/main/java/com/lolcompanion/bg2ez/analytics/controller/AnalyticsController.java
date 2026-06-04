package com.lolcompanion.bg2ez.analytics.controller;

import com.lolcompanion.bg2ez.account.repository.SummonerRepository;
import com.lolcompanion.bg2ez.analytics.converter.ChampionStatsConverter;
import com.lolcompanion.bg2ez.analytics.converter.RoleStatsConverter;
import com.lolcompanion.bg2ez.analytics.entity.ChampionStats;
import com.lolcompanion.bg2ez.analytics.entity.RoleStats;
import com.lolcompanion.bg2ez.analytics.model.ChampionStatsModel;
import com.lolcompanion.bg2ez.analytics.model.RoleStatsModel;
import com.lolcompanion.bg2ez.analytics.service.AnalyticsService;
import com.lolcompanion.bg2ez.ranked.converter.RankedEntryConverter;
import com.lolcompanion.bg2ez.ranked.model.RankedEntryModel;
import com.lolcompanion.bg2ez.ranked.repository.RankedEntryRepository;
import org.springframework.web.bind.annotation.*;

import javax.management.relation.Role;
import java.util.List;

@RestController
@RequestMapping("/api/v1/me")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final SummonerRepository summonerRepository;
    private final ChampionStatsConverter championStatsConverter;
    private final RoleStatsConverter roleStatsConverter;
    private final RankedEntryRepository rankedEntryRepository;
    private final RankedEntryConverter rankedEntryConverter;

    public AnalyticsController(AnalyticsService analyticsService,
                               SummonerRepository summonerRepository,
                               ChampionStatsConverter championStatsConverter,
                               RoleStatsConverter roleStatsConverter,
                               RankedEntryRepository rankedEntryRepository,
                               RankedEntryConverter rankedEntryConverter) {
        this.analyticsService = analyticsService;
        this.summonerRepository = summonerRepository;
        this.championStatsConverter = championStatsConverter;
        this.roleStatsConverter = roleStatsConverter;
        this.rankedEntryRepository = rankedEntryRepository;
        this.rankedEntryConverter = rankedEntryConverter;
    }

    private String getLinkedPuuid() {
        return summonerRepository.findAll()
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No account linked"))
                .getPuuid();
    }

    @PostMapping("/analytics/compute")
    public void compute() {
        analyticsService.computeForPlayer(getLinkedPuuid());
    }

    @GetMapping("/champions")
    public List<ChampionStatsModel> champions() {
        return analyticsService.getChampionStats(getLinkedPuuid())
                .stream()
                .map(championStatsConverter::entityToModel)
                .toList();
    }

    @GetMapping("/roles")
    public List<RoleStatsModel> roles() {
        return analyticsService.getRoleStats(getLinkedPuuid())
                .stream()
                .map(roleStatsConverter::entityToModel)
                .toList();
    }

    @GetMapping("/ranked")
    public List<RankedEntryModel> ranked() {
        return rankedEntryRepository.findByPuuidOrderByRecordedAtDesc(getLinkedPuuid())
                .stream()
                .map(rankedEntryConverter::entityToModel)
                .toList();


    }
}