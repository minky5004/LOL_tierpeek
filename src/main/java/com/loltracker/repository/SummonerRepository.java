package com.loltracker.repository;

import com.loltracker.entity.Summoner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SummonerRepository extends JpaRepository<Summoner, Long> {
    Optional<Summoner> findByPuuid(String puuid);

    Optional<Summoner> findByGameNameAndTagLine(String gameName, String tagLine);

    List<Summoner> findAllByPlatform(String platform);
}
