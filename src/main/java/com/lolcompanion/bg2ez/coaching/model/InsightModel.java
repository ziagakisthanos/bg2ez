package com.lolcompanion.bg2ez.coaching.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record InsightModel(
        String insightType,
        String subject,
        Integer matchWindow,
        OffsetDateTime generatedAt,
        String modelUsed,
        List<InsightSectionModel> sections
) {}