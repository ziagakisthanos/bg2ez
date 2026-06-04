package com.lolcompanion.bg2ez.live.model;

import java.util.List;

public record LiveGameModel(
        String gameId,
        String gameMode,
        String queueLabel,
        Long gameLength,
        List<LivePlayerModel> allies,
        List<LivePlayerModel> enemies
) {}