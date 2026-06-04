package com.lolcompanion.bg2ez.riot.controller;

import com.lolcompanion.bg2ez.riot.service.SyncService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class SyncController {

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping("/sync")
    public SyncService.SyncResult sync(
            @RequestParam(defaultValue = "20") int count) {
        if (count < 1 || count > 100) {
            throw new IllegalArgumentException("count must be between 1 and 100");
        }
        return syncService.sync(count);
    }
}