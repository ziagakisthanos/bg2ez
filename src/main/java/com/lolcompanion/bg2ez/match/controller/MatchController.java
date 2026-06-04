package com.lolcompanion.bg2ez.match.controller;

import com.lolcompanion.bg2ez.match.model.MatchSummaryModel;
import com.lolcompanion.bg2ez.match.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/me")
public class MatchController {

    private final MatchService matchService;

    @GetMapping("/matches")
    public List<MatchSummaryModel> matches(
            @RequestParam(defaultValue = "20") int limit) {
        return matchService.getMatchHistory(limit);
    }
}