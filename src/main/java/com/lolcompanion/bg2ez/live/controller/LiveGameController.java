package com.lolcompanion.bg2ez.live.controller;

import com.lolcompanion.bg2ez.live.model.LiveGameModel;
import com.lolcompanion.bg2ez.live.service.LiveGameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class LiveGameController {

    private final LiveGameService liveGameService;

    public LiveGameController(LiveGameService liveGameService) {
        this.liveGameService = liveGameService;
    }

    @GetMapping("/live")
    public ResponseEntity<LiveGameModel> getLiveGame() {
        try {
            return ResponseEntity.ok(liveGameService.getLiveGame());
        } catch (Exception e) {
            // Not in a game
            return ResponseEntity.noContent().build();
        }
    }
}