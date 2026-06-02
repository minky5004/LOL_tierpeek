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
     * 여러 소환사의 최신 랭크를 한 번에 조회합니다.
     * N+1 쿼리 문제를 해결하기 위한 배치 메서드입니다.
     *
     * @param puuids 소환사 고유 식별자 목록
     * @param queueType 큐 타입
     * @return puuid를 키로 하는 최신 랭크 스냅샷 Map (없으면 제외)
     */
    @Transactional(readOnly = true)
    public Map<String, RankSnapshot> getLatestRanksForPuuids(List<String> puuids, String queueType) {
        Map<String, RankSnapshot> resultMap = new HashMap<>();

        for (String puuid : puuids) {
            Optional<RankSnapshot> latestRank = getLatestRank(puuid, queueType);
            latestRank.ifPresent(rank -> resultMap.put(puuid, rank));
        }

        return resultMap;
    }
}
