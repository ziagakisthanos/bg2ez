package com.lolcompanion.bg2ez.live.model;

public record LivePlayerModel(
        String puuid,
        String summonerName,
        Integer championId,
        String tier,
        String rank,
        Integer leaguePoints,
        Integer wins,
        Integer losses,
        Double winRate,
        Boolean isYou
) {}