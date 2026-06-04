package com.lolcompanion.bg2ez.coaching.controller;

import com.lolcompanion.bg2ez.coaching.model.AnalyseRequest;
import com.lolcompanion.bg2ez.coaching.model.InsightModel;
import com.lolcompanion.bg2ez.coaching.service.InsightGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coach")
public class CoachController {

    private final InsightGenerationService insightGenerationService;

    @PostMapping("/analyse")
    public InsightModel analyse(@RequestBody AnalyseRequest request) {
        return insightGenerationService.analyse(request);
    }

    @GetMapping("/insights")
    public List<InsightModel> insights() {
        return insightGenerationService.getInsights();
    }

    @GetMapping("/insights/latest")
    public InsightModel latest(@RequestParam(defaultValue = "session") String type) {
        return insightGenerationService.getLatestByType(type);
    }
}