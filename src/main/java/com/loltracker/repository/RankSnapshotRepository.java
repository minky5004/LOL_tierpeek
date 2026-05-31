package com.loltracker.repository;

import com.loltracker.entity.RankSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RankSnapshotRepository extends JpaRepository<RankSnapshot, Long> {

    List<RankSnapshot> findByPuuidAndQueueTypeOrderByRecordedAtDesc(String puuid, String queueType);

    @Query("SELECT rs FROM RankSnapshot rs WHERE rs.puuid = :puuid AND rs.queueType = :queueType " +
           "AND rs.recordedAt BETWEEN :startDate AND :endDate ORDER BY rs.recordedAt DESC")
    List<RankSnapshot> findByDateRange(
            @Param("puuid") String puuid,
            @Param("queueType") String queueType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    RankSnapshot findFirstByPuuidAndQueueTypeOrderByRecordedAtDesc(String puuid, String queueType);
}
