package com.lolcompanion.bg2ez.ranked.converter;

import com.lolcompanion.bg2ez.ranked.entity.RankedEntry;
import com.lolcompanion.bg2ez.ranked.model.RankedEntryModel;
import org.springframework.stereotype.Component;

@Component
public class RankedEntryConverter {

    public RankedEntryModel entityToModel(RankedEntry entity) {
        return new RankedEntryModel(
                entity.getQueueType(),
                entity.getTier(),
                entity.getRank(),
                entity.getLeaguePoints(),
                entity.getWins(),
                entity.getLosses(),
                entity.getRecordedAt()
        );
    }

    public RankedEntry modelToEntity(RankedEntryModel model, String puuid) {
        RankedEntry entity = new RankedEntry();
        entity.setPuuid(puuid);
        entity.setQueueType(model.queueType());
        entity.setTier(model.tier());
        entity.setRank(model.rank());
        entity.setLeaguePoints(model.leaguePoints());
        entity.setWins(model.wins());
        entity.setLosses(model.losses());
        entity.setRecordedAt(model.recordedAt());
        return entity;
    }
}