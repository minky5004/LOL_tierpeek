package com.tierpeek.controller;

import com.tierpeek.dto.ApiResponse;
import com.tierpeek.dto.MatchRecordDto;
import com.tierpeek.entity.MatchRecord;
import com.tierpeek.service.MatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/friends/{puuid}/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    private static final int DEFAULT_COUNT = 20;

    @GetMapping
    public ApiResponse<List<MatchRecordDto>> getMatches(
            @PathVariable String puuid,
            @RequestParam(defaultValue = "20") int count) {
        log.info("매치 기록 조회: {} (count: {})", puuid, count);

        Pageable pageable = PageRequest.of(0, count);
        List<MatchRecord> records = matchService.getRecentMatches(puuid, pageable);

        List<MatchRecordDto> matches = records.stream()
                .map(r -> MatchRecordDto.builder()
                        .matchId(r.getMatchId())
                        .champion(r.getChampion())
                        .win(r.getWin())
                        .kills(r.getKills())
                        .deaths(r.getDeaths())
                        .assists(r.getAssists())
                        .queueId(r.getQueueId())
                        .gameCreation(r.getGameCreation())
                        .gameDuration(r.getGameDuration())
                        .build())
                .collect(Collectors.toList());

        return ApiResponse.success("매치 기록 조회 성공", matches);
    }
}
