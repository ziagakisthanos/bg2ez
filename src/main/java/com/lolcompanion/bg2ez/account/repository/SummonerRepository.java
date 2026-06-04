package com.lolcompanion.bg2ez.account.repository;

import com.lolcompanion.bg2ez.account.entity.Summoner;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SummonerRepository extends JpaRepository<Summoner, UUID> {
    Optional<Summoner> findByPuuid(String puuid);
    Optional<Summoner> findByGameNameAndTagLine(String gameName, String tagLine);
}