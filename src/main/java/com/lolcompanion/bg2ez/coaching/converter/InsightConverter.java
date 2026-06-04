package com.lolcompanion.bg2ez.coaching.converter;

import com.lolcompanion.bg2ez.coaching.entity.Insight;
import com.lolcompanion.bg2ez.coaching.model.InsightModel;
import com.lolcompanion.bg2ez.coaching.model.InsightSectionModel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InsightConverter {

    public InsightModel entityToModel(Insight insight) {
        List<InsightSectionModel> sections = insight.getSections() == null
                ? List.of()
                : insight.getSections().stream()
                .sorted((a, b) -> Integer.compare(
                        a.getDisplayOrder(), b.getDisplayOrder()))
                .map(s -> new InsightSectionModel(
                        s.getSectionType(),
                        s.getContent(),
                        s.getDisplayOrder()))
                .toList();

        return new InsightModel(
                insight.getInsightType(),
                insight.getSubject(),
                insight.getMatchWindow(),
                insight.getGeneratedAt(),
                insight.getModelUsed(),
                sections
        );
    }
}