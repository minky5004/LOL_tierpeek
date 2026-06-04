package com.tierpeek.service;

import com.tierpeek.client.RiotApiClient;
import com.tierpeek.dto.riot.MatchDto;
import com.tierpeek.entity.MatchRecord;
import com.tierpeek.repository.MatchRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {

    private final RiotApiClient riotApiClient;
    private final MatchRecordRepository matchRecordRepository;
    private final MatchRecordSaver matchRecordSaver;
    private final FriendService friendService;

    /**
     * 소환사의 매치 기록을 동기화합니다.
     * 최신 매치 ID를 조회하고, 아직 저장되지 않은 매치의 상세 정보를 가져와 저장합니다.
     *
     * @param puuid 소환사 고유 식별자
     * @param count 조회할 매치 개수
     */
    @Transactional
    public void syncMatches(String puuid, int count) {
        log.info("매치 동기화 시작: {} (최대 {} 경기)", puuid, count);

        var summoner = friendService.getFriendByPuuid(puuid);
        List<String> matchIds = riotApiClient.getMatchIdsByPuuid(puuid, summoner.getPlatform(), 0, count);
        log.debug("조회된 매치 ID: {} 개", matchIds.size());

        int syncCount = 0;

        for (String matchId : matchIds) {
            if (matchRecordRepository.existsByMatchIdAndPuuid(matchId, puuid)) {
                log.debug("이미 저장된 매치: {}", matchId);
                continue;
            }

            try {
                MatchDto matchDto = riotApiClient.getMatchDetail(matchId, summoner.getPlatform());
                matchRecordSaver.saveMatchRecord(matchId, puuid, matchDto);
                syncCount++;
            } catch (Exception e) {
                log.error("매치 상세 저장 실패: {} (puuid: {})", matchId, puuid, e);
            }
        }

        log.info("매치 동기화 완료: {} 경기 저장됨", syncCount);
    }


    @Transactional(readOnly = true)
    public List<MatchRecord> getRecentMatches(String puuid, Pageable pageable) {
        return matchRecordRepository.findByPuuidOrderByGameCreationDesc(puuid, pageable);
    }

    /**
     * 여러 소환사의 최근 매치를 한 번의 배치 쿼리로 조회합니다.
     * 단일 쿼리로 모든 데이터를 조회하여 N+1 문제를 완전히 해결하고,
     * 결과를 puuid별로 limit만큼 그룹핑하여 반환합니다.
     *
     * @param puuids 소환사 고유 식별자 목록
     * @param count 소환사당 조회할 매치 개수 (범위: 1~100)
     * @return puuid를 키로 하는 최근 매치 목록 Map (소환사당 최대 count개)
     * @throws IllegalArgumentException puuids가 비어있거나 count가 범위 외인 경우
     */
    @Transactional(readOnly = true)
    public Map<String, List<MatchRecord>> getRecentMatchesForPuuids(List<String> puuids, int count) {
        if (puuids == null || puuids.isEmpty()) {
            return Map.of();
        }
        if (count <= 0 || count > 100) {
            throw new IllegalArgumentException("count must be between 1 and 100");
        }

        Pageable pageable = PageRequest.of(0, count * puuids.size());
        List<MatchRecord> allMatches = matchRecordRepository.findRecentByPuuidsOrderByGameCreationDesc(puuids, pageable);

        Map<String, List<MatchRecord>> resultMap = new HashMap<>();
        for (String puuid : puuids) {
            resultMap.put(puuid, List.of());
        }

        for (MatchRecord match : allMatches) {
            String puuid = match.getPuuid();
            List<MatchRecord> puuidMatches = resultMap.get(puuid);
            if (puuidMatches != null && puuidMatches.size() < count) {
                List<MatchRecord> newList = new java.util.ArrayList<>(puuidMatches);
                newList.add(match);
                resultMap.put(puuid, newList);
            }
        }

        return resultMap;
    }
}
