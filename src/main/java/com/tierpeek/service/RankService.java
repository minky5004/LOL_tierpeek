package com.tierpeek.service;

import com.tierpeek.client.RiotApiClient;
import com.tierpeek.dto.LpPointDto;
import com.tierpeek.dto.riot.LeagueEntryDto;
import com.tierpeek.entity.RankSnapshot;
import com.tierpeek.repository.RankSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankService {

    private final RiotApiClient riotApiClient;
    private final RankSnapshotRepository rankSnapshotRepository;

    /**
     * 소환사의 현재 랭크 스냅샷을 수집합니다.
     * 모든 큐 타입(솔로, 자유, 3v3 등)의 랭크 정보를 조회하여 DB에 저장합니다.
     *
     * @param puuid 소환사 고유 식별자
     * @param platform 플랫폼 (kr, na 등)
     */
    @Transactional
    public void collectRankSnapshot(String puuid, String platform) {
        log.info("랭크 스냅샷 수집 시작: {}", puuid);

        List<LeagueEntryDto> entries = riotApiClient.getLeagueEntriesByPuuid(puuid, platform);

        for (LeagueEntryDto entry : entries) {
            RankSnapshot snapshot = RankSnapshot.builder()
                    .puuid(puuid)
                    .queueType(entry.getQueueType())
                    .tier(entry.getTier())
                    .division(entry.getRank())
                    .leaguePoints(entry.getLeaguePoints())
                    .wins(entry.getWins())
                    .losses(entry.getLosses())
                    .recordedAt(LocalDateTime.now())
                    .build();

            rankSnapshotRepository.save(snapshot);
            log.debug("스냅샷 저장: {} - {} {}", puuid, entry.getTier(), entry.getRank());
        }

        log.info("랭크 스냅샷 수집 완료: {} ({} 큐)", puuid, entries.size());
    }

    @Transactional(readOnly = true)
    public List<LpPointDto> getLpHistory(String puuid, String queueType) {
        List<RankSnapshot> snapshots = rankSnapshotRepository
                .findByPuuidAndQueueTypeOrderByRecordedAtDesc(puuid, queueType);

        return snapshots.stream()
                .map(s -> LpPointDto.builder()
                        .recordedAt(s.getRecordedAt())
                        .tier(s.getTier())
                        .division(s.getDivision())
                        .leaguePoints(s.getLeaguePoints())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<RankSnapshot> getLatestRank(String puuid, String queueType) {
        return Optional.ofNullable(
                rankSnapshotRepository.findFirstByPuuidAndQueueTypeOrderByRecordedAtDesc(puuid, queueType)
        );
    }

    /**
     * 여러 소환사의 최신 랭크를 한 번의 배치 쿼리로 조회합니다.
     * 단일 쿼리로 모든 데이터를 조회하여 N+1 문제를 완전히 해결합니다.
     *
     * @param puuids 소환사 고유 식별자 목록
     * @param queueType 큐 타입 (RANKED_SOLO_5x5, RANKED_FLEX_SR 등)
     * @return puuid를 키로 하는 최신 랭크 스냅샷 Map (없으면 제외)
     * @throws IllegalArgumentException puuids가 비어있거나 queueType이 null/blank인 경우
     */
    @Transactional(readOnly = true)
    public Map<String, RankSnapshot> getLatestRanksForPuuids(List<String> puuids, String queueType) {
        if (puuids == null || puuids.isEmpty()) {
            return Map.of();
        }
        if (queueType == null || queueType.isBlank()) {
            throw new IllegalArgumentException("queueType must not be null or blank");
        }

        List<RankSnapshot> snapshots = rankSnapshotRepository.findLatestByPuuidsAndQueueType(puuids, queueType);

        return snapshots.stream()
                .collect(Collectors.toMap(RankSnapshot::getPuuid, rs -> rs));
    }
}
