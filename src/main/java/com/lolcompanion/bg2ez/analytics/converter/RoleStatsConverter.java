package com.lolcompanion.bg2ez.analytics.converter;

import com.lolcompanion.bg2ez.analytics.entity.RoleStats;
import com.lolcompanion.bg2ez.analytics.entity.RoleStatsId;
import com.lolcompanion.bg2ez.analytics.model.RoleStatsModel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class RoleStatsConverter {

    public RoleStatsModel entityToModel(RoleStats entity) {
        BigDecimal winRate = entity.getGamesPlayed() > 0
                ? BigDecimal.valueOf(entity.getWins())
                .divide(BigDecimal.valueOf(entity.getGamesPlayed()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new RoleStatsModel(
                entity.getId().getRole(),
                entity.getGamesPlayed(),
                entity.getWins(),
                entity.getLosses(),
                winRate,
                entity.getAvgKda()
        );
    }

    public RoleStats modelToEntity(RoleStatsModel model, String puuid) {
        RoleStatsId id = new RoleStatsId();
        id.setPuuid(puuid);
        id.setRole(model.role());

        RoleStats entity = new RoleStats();
        entity.setId(id);
        entity.setGamesPlayed(model.gamesPlayed());
        entity.setWins(model.wins());
        entity.setLosses(model.losses());
        entity.setAvgKda(model.avgKda());
        return entity;
    }
}