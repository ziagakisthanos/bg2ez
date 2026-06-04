package com.lolcompanion.bg2ez.analytics.converter;

import com.lolcompanion.bg2ez.analytics.entity.ChampionStats;
import com.lolcompanion.bg2ez.analytics.entity.ChampionStatsId;
import com.lolcompanion.bg2ez.analytics.model.ChampionStatsModel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ChampionStatsConverter {

    public ChampionStatsModel entityToModel(ChampionStats entity) {
        BigDecimal winRate = entity.getGamesPlayed() > 0
                ? BigDecimal.valueOf(entity.getWins())
                .divide(BigDecimal.valueOf(entity.getGamesPlayed()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new ChampionStatsModel(
                entity.getId().getChampionName(),
                entity.getGamesPlayed(),
                entity.getWins(),
                entity.getLosses(),
                winRate,
                entity.getAvgKda(),
                entity.getAvgCs(),
                entity.getAvgDamage(),
                entity.getAvgVision()
        );
    }

    public ChampionStats modelToEntity(ChampionStatsModel model, String puuid) {
        ChampionStatsId id = new ChampionStatsId();
        id.setPuuid(puuid);
        id.setChampionName(model.championName());

        ChampionStats entity = new ChampionStats();
        entity.setId(id);
        entity.setGamesPlayed(model.gamesPlayed());
        entity.setWins(model.wins());
        entity.setLosses(model.losses());
        entity.setAvgKda(model.avgKda());
        entity.setAvgCs(model.avgCs());
        entity.setAvgDamage(model.avgDamage());
        entity.setAvgVision(model.avgVision());
        return entity;
    }
}