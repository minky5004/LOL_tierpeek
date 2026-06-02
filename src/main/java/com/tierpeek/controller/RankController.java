package com.tierpeek.controller;

import com.tierpeek.dto.ApiResponse;
import com.tierpeek.dto.LpPointDto;
import com.tierpeek.service.RankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 친구의 랭크 이력 조회 API 엔드포인트입니다.
 * 수집된 LP 스냅샷을 시계열로 조회하여 그래프 데이터를 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/friends/{puuid}/rank-history")
@RequiredArgsConstructor
public class RankController {

    private final RankService rankService;

    private static final String DEFAULT_QUEUE = "RANKED_SOLO_5x5";

    /**
     * 친구의 랭크 이력(LP 그래프)을 조회합니다.
     *
     * @param puuid 소환사 고유 식별자
     * @param queue 큐 타입 (기본값: RANKED_SOLO_5x5, RANKED_FLEX_SR 등)
     * @return LP 포인트 시계열 데이터
     */
    @GetMapping
    public ApiResponse<List<LpPointDto>> getRankHistory(
            @PathVariable String puuid,
            @RequestParam(defaultValue = DEFAULT_QUEUE) String queue) {
        log.info("랭크 이력 조회: {} (queue: {})", puuid, queue);

        List<LpPointDto> history = rankService.getLpHistory(puuid, queue);
        return ApiResponse.success("랭크 이력 조회 성공", history);
    }
}
