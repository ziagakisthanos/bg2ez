package com.lolcompanion.bg2ez.account.converter;

import com.lolcompanion.bg2ez.account.entity.Summoner;
import com.lolcompanion.bg2ez.account.model.SummonerModel;
import org.springframework.stereotype.Component;

@Component
public class SummonerConverter {

    public SummonerModel entityToModel(Summoner entity) {
        return new SummonerModel(
                entity.getGameName(),
                entity.getTagLine(),
                entity.getProfileIconId(),
                entity.getSummonerLevel(),
                entity.getLinkedAt(),
                entity.getLastSyncedAt()
        );
    }

    public Summoner modelToEntity(SummonerModel model) {
        Summoner entity = new Summoner();
        entity.setGameName(model.gameName());
        entity.setTagLine(model.tagLine());
        entity.setProfileIconId(model.profileIconId());
        entity.setSummonerLevel(model.summonerLevel());
        entity.setLinkedAt(model.linkedAt());
        entity.setLastSyncedAt(model.lastSyncedAt());
        return entity;
    }
}