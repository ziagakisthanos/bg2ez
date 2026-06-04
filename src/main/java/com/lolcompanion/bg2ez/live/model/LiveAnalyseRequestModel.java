package com.lolcompanion.bg2ez.live.model;

import com.lolcompanion.bg2ez.coaching.service.OpponentAnalysisService;
import java.util.List;

public record LiveAnalyseRequestModel(
        String gameId,
        List<OpponentAnalysisService.OpponentRequest> enemies
) {}