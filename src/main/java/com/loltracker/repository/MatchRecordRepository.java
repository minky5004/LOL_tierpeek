package com.loltracker.repository;

import com.loltracker.entity.MatchRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRecordRepository extends JpaRepository<MatchRecord, Long> {

    Optional<MatchRecord> findByMatchIdAndPuuid(String matchId, String puuid);

    List<MatchRecord> findByPuuidOrderByGameCreationDesc(String puuid, Pageable pageable);

    List<MatchRecord> findByPuuid(String puuid);

    boolean existsByMatchIdAndPuuid(String matchId, String puuid);
}
