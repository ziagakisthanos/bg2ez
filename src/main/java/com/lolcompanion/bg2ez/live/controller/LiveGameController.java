package com.lolcompanion.bg2ez.live.controller;

import com.lolcompanion.bg2ez.coaching.model.OpponentInsightModel;
import com.lolcompanion.bg2ez.coaching.service.OpponentAnalysisService;
import com.lolcompanion.bg2ez.live.model.LiveAnalyseRequestModel;
import com.lolcompanion.bg2ez.live.model.LiveGameModel;
import com.lolcompanion.bg2ez.live.service.LiveGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class LiveGameController {

    private final LiveGameService liveGameService;
    private final OpponentAnalysisService opponentAnalysisService;

    @GetMapping("/live")
    public ResponseEntity<LiveGameModel> getLiveGame() {
        try {
            return ResponseEntity.ok(liveGameService.getLiveGame());
        } catch (Exception e) {
            // Not in a game
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping("/live/analyse")
    public ResponseEntity<List<OpponentInsightModel>> analyseEnemies(
            @RequestBody LiveAnalyseRequestModel request) {
        try {
            List<OpponentInsightModel> results = opponentAnalysisService
                    .analyseEnemies(request.gameId(), request.enemies());
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}