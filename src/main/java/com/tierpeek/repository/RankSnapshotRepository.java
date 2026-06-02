package com.tierpeek.repository;

import com.tierpeek.entity.RankSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    /**
     * 여러 소환사의 최신 랭크 스냅샷을 한 번의 쿼리로 조회합니다.
     * 각 puuid별로 가장 최신 recordedAt를 가진 스냅샷만 반환합니다.
     *
     * @param puuids 소환사 고유 식별자 목록
     * @param queueType 큐 타입
     * @return puuid를 키로 하는 최신 랭크 스냅샷 Map
     */
    @Query("SELECT rs FROM RankSnapshot rs " +
           "WHERE rs.puuid IN :puuids AND rs.queueType = :queueType " +
           "AND rs.recordedAt = (" +
           "  SELECT MAX(rs2.recordedAt) FROM RankSnapshot rs2 " +
           "  WHERE rs2.puuid = rs.puuid AND rs2.queueType = :queueType" +
           ")")
    List<RankSnapshot> findLatestByPuuidsAndQueueType(
            @Param("puuids") List<String> puuids,
            @Param("queueType") String queueType);
}
