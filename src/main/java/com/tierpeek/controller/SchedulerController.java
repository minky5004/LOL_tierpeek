package com.tierpeek.controller;

import com.tierpeek.dto.ApiResponse;
import com.tierpeek.scheduler.MatchSyncJob;
import com.tierpeek.scheduler.RankSnapshotCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
public class SchedulerController {

    private final RankSnapshotCollector rankSnapshotCollector;
    private final MatchSyncJob matchSyncJob;

    @PostMapping("/refresh")
    public ApiResponse<String> runRefresh() {
        log.info("수동 전체 갱신 요청 (랭크 + 매치)");
        StringBuilder result = new StringBuilder();

        try {
            rankSnapshotCollector.collectRankSnapshots();
            result.append("✓ 랭크 수집 완료");
        } catch (Exception e) {
            log.error("랭크 수집 중 오류", e);
            result.append("✗ 랭크 수집 실패");
        }

        result.append(" | ");

        try {
            matchSyncJob.syncMatches();
            result.append("✓ 매치 동기화 완료");
        } catch (Exception e) {
            log.error("매치 동기화 중 오류", e);
            result.append("✗ 매치 동기화 실패");
        }

        return ApiResponse.success(result.toString());
    }
}