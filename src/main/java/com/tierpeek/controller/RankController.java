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

@Slf4j
@RestController
@RequestMapping("/api/friends/{puuid}/rank-history")
@RequiredArgsConstructor
public class RankController {

    private final RankService rankService;

    private static final String DEFAULT_QUEUE = "RANKED_SOLO_5x5";

    @GetMapping
    public ApiResponse<List<LpPointDto>> getRankHistory(
            @PathVariable String puuid,
            @RequestParam(defaultValue = "RANKED_SOLO_5x5") String queue) {
        log.info("랭크 이력 조회: {} (queue: {})", puuid, queue);

        List<LpPointDto> history = rankService.getLpHistory(puuid, queue);
        return ApiResponse.success("랭크 이력 조회 성공", history);
    }
}
