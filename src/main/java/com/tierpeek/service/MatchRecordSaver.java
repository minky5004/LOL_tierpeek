package com.tierpeek.service;

import com.tierpeek.dto.riot.MatchDto;
import com.tierpeek.entity.MatchRecord;
import com.tierpeek.repository.MatchRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchRecordSaver {

    private final MatchRecordRepository matchRecordRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveMatchRecord(String matchId, String puuid, MatchDto matchDto) {
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
                ZoneOffset.UTC
        );
    }
}