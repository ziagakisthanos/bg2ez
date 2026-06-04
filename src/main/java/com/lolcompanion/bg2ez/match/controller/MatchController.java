package com.lolcompanion.bg2ez.match.controller;

import com.lolcompanion.bg2ez.match.model.MatchSummaryModel;
import com.lolcompanion.bg2ez.match.service.MatchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/me")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping("/matches")
    public List<MatchSummaryModel> matches(
            @RequestParam(defaultValue = "20") int limit) {
        return matchService.getMatchHistory(limit);
    }
}