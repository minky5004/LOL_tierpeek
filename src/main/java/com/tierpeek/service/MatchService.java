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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {

    private final RiotApiClient riotApiClient;
    private final MatchRecordRepository matchRecordRepository;

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
                saveMatchRecord(matchId, puuid, matchDto);
                syncCount++;
            } catch (Exception e) {
                log.error("매치 상세 저장 실패: {} (puuid: {})", matchId, puuid, e);
            }
        }

        log.info("매치 동기화 완료: {} 경기 저장됨", syncCount);
    }

    @Transactional
    private void saveMatchRecord(String matchId, String puuid, MatchDto matchDto) {
        MatchDto.ParticipantDto participant = findParticipant(puuid, matchDto);

        if (participant == null) {
            log.warn("매치에서 플레이어를 찾을 수 없음: {} in {}", puuid, matchId);
            return;
        }

        LocalDateTime gameCreation = convertTimestamp(matchDto.getInfo().getGameCreation());

        MatchRecord record = MatchRecord.builder()
                .matchId(matchId)
                .puuid(puuid)
                .champion(participant.getChampionName())
                .win(participant.isWin())
                .kills(participant.getKills())
                .deaths(participant.getDeaths())
                .assists(participant.getAssists())
                .queueId(matchDto.getInfo().getQueueId())
                .gameCreation(gameCreation)
                .gameDuration((int) matchDto.getInfo().getGameDuration())
                .build();

        matchRecordRepository.save(record);
        log.debug("매치 저장: {} - {} {}", matchId, participant.getChampionName(), participant.isWin() ? "승리" : "패배");
    }

    private MatchDto.ParticipantDto findParticipant(String puuid, MatchDto matchDto) {
        return matchDto.getInfo().getParticipants().stream()
                .filter(p -> p.getPuuid().equals(puuid))
                .findFirst()
                .orElse(null);
    }

    private LocalDateTime convertTimestamp(long milliseconds) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(milliseconds),
                ZoneId.systemDefault()
        );
    }

    @Transactional(readOnly = true)
    public List<MatchRecord> getRecentMatches(String puuid, Pageable pageable) {
        return matchRecordRepository.findByPuuidOrderByGameCreationDesc(puuid, pageable);
    }
}
