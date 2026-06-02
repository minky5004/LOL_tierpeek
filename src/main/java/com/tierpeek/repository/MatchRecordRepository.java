package com.tierpeek.repository;

import com.tierpeek.entity.MatchRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * 여러 소환사의 최근 매치를 한 번의 쿼리로 조회합니다.
     * 각 puuid의 최근 매치부터 Pageable 범위만큼 반환합니다.
     *
     * @param puuids 소환사 고유 식별자 목록
     * @param pageable 페이지 정보 (offset, limit)
     * @return 모든 puuid의 최근 매치 목록 (gameCreation DESC 정렬)
     */
    @Query("SELECT mr FROM MatchRecord mr WHERE mr.puuid IN :puuids ORDER BY mr.gameCreation DESC")
    List<MatchRecord> findRecentByPuuidsOrderByGameCreationDesc(
            @Param("puuids") List<String> puuids,
            Pageable pageable);
}
