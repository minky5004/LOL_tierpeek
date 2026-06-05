package com.tierpeek.controller;

import com.tierpeek.dto.ApiResponse;
import com.tierpeek.exception.SchedulerAlreadyRunningException;
import com.tierpeek.scheduler.MatchSyncJob;
import com.tierpeek.scheduler.RankSnapshotCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
public class SchedulerController {

    private final RankSnapshotCollector rankSnapshotCollector;
    private final MatchSyncJob matchSyncJob;

    @Value("${scheduler.api.key:}")
    private String schedulerApiKey;

    private final AtomicBoolean isRefreshing = new AtomicBoolean(false);

    @PostMapping("/refresh")
    public ApiResponse<String> runRefresh(@RequestHeader(value = "X-Scheduler-Key", required = false) String apiKey) {
        // API 키 검증 (설정되어 있는 경우)
        if (!schedulerApiKey.isEmpty() && !schedulerApiKey.equals(apiKey)) {
            log.warn("갱신 요청 - 인증 실패");
            return ApiResponse.failure("인증 실패");
        }

        // 동시 실행 방지
        if (!isRefreshing.compareAndSet(false, true)) {
            log.warn("갱신이 이미 진행 중입니다");
            return ApiResponse.failure("갱신이 이미 진행 중입니다. 잠시 후 다시 시도해주세요.");
        }

        try {
            log.info("수동 전체 갱신 요청 (랭크 + 매치)");
            StringBuilder result = new StringBuilder();
            boolean rankSuccess = false;
            boolean matchSuccess = false;

            try {
                rankSnapshotCollector.collectRankSnapshots();
                result.append("✓ 랭크 수집 완료");
                rankSuccess = true;
            } catch (SchedulerAlreadyRunningException e) {
                log.warn("랭크 수집 건너뜀: {}", e.getMessage());
                result.append("⏭️ 랭크 수집 건너뜀(이미 진행 중)");
            } catch (Exception e) {
                log.error("랭크 수집 중 오류", e);
                result.append("✗ 랭크 수집 실패");
            }

            result.append(" | ");

            try {
                matchSyncJob.syncMatches();
                result.append("✓ 매치 동기화 완료");
                matchSuccess = true;
            } catch (SchedulerAlreadyRunningException e) {
                log.warn("매치 동기화 건너뜀: {}", e.getMessage());
                result.append("⏭️ 매치 동기화 건너뜀(이미 진행 중)");
            } catch (Exception e) {
                log.error("매치 동기화 중 오류", e);
                result.append("✗ 매치 동기화 실패");
            }

            // 둘 다 성공하면 success, 하나라도 실패하면 failure
            if (rankSuccess && matchSuccess) {
                return ApiResponse.success("갱신 완료", result.toString());
            } else {
                return ApiResponse.failure(result.toString());
            }
        } finally {
            isRefreshing.set(false);
        }
    }
}