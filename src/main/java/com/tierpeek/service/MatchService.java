package com.tierpeek.service;

import com.tierpeek.client.RiotApiClient;
import com.tierpeek.dto.riot.MatchDto;
import com.tierpeek.entity.MatchRecord;
import com.tierpeek.repository.MatchRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {

    private final RiotApiClient riotApiClient;
    private final MatchRecordRepository matchRecordRepository;
    private final MatchRecordSaver matchRecordSaver;

    @Transactional
    public void syncMatches(String puuid, int count) {
        log.info("매치 동기화 시작: {} (최대 {} 경기)", puuid, count);

        List<String> matchIds = riotApiClient.getMatchIdsByPuuid(puuid, 0, count);
        log.debug("조회된 매치 ID: {} 개", matchIds.size());

        int syncCount = 0;

        for (String matchId : matchIds) {
            if (matchRecordRepository.existsByMatchIdAndPuuid(matchId, puuid)) {
                log.debug("이미 저장된 매치: {}", matchId);
                continue;
            }

            try {
                MatchDto matchDto = riotApiClient.getMatchDetail(matchId);
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
}
