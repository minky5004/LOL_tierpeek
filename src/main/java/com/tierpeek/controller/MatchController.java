package com.tierpeek.controller;

import com.tierpeek.config.CacheConfig;
import com.tierpeek.dto.ApiResponse;
import com.tierpeek.dto.MatchRecordDto;
import com.tierpeek.entity.MatchRecord;
import com.tierpeek.service.MatchService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 친구의 매치 기록 조회 API 엔드포인트입니다.
 * 최근 매치 기록을 페이지네이션으로 조회할 수 있습니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/friends/{puuid}/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    private static final String DEFAULT_COUNT = "20";

    /**
     * 친구의 최근 매치 기록을 조회합니다.
     * Redis에 1시간 TTL로 캐시됩니다.
     *
     * @param puuid 소환사 고유 식별자
     * @param count 조회할 매치 개수 (기본값: 20, 범위: 1~100)
     * @return 매치 기록 목록
     */
    @Cacheable(value = CacheConfig.MATCHES_CACHE, key = "#puuid + ':' + #count")
    @GetMapping
    public ApiResponse<List<MatchRecordDto>> getMatches(
            @PathVariable String puuid,
            @Min(value = 1, message = "count는 1 이상이어야 합니다")
            @Max(value = 100, message = "count는 100 이하여야 합니다")
            @RequestParam(defaultValue = DEFAULT_COUNT) int count) {
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
