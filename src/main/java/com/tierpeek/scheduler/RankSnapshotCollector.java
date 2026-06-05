package com.tierpeek.scheduler;

import com.tierpeek.config.CacheConfig;
import com.tierpeek.entity.Summoner;
import com.tierpeek.exception.RiotApiException;
import com.tierpeek.exception.SchedulerAlreadyRunningException;
import com.tierpeek.service.FriendService;
import com.tierpeek.service.RankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankSnapshotCollector {

    private final FriendService friendService;
    private final RankService rankService;

    @Value("${rank.snapshot.fixedRateMs:1800000}")
    private long fixedRateMs;

    private final AtomicBoolean isCollecting = new AtomicBoolean(false);

    /**
     * 등록된 친구들의 현재 랭크 스냅샷을 주기적으로 수집합니다.
     * 수집 후 관련 캐시를 초기화합니다.
     */
    @CacheEvict(value = {CacheConfig.DASHBOARD_CACHE, CacheConfig.RANK_HISTORY_CACHE}, allEntries = true)
    @Scheduled(initialDelay = 0, fixedRateString = "${rank.snapshot.fixedRateMs:1800000}")
    public void collectRankSnapshots() {
        // 동시 실행 방지
        if (!isCollecting.compareAndSet(false, true)) {
            throw new SchedulerAlreadyRunningException("랭크 스냅샷 수집이 이미 진행 중입니다");
        }

        try {
            log.info("========== 랭크 스냅샷 수집 시작 ==========");

            List<Summoner> friends = friendService.getAllFriends();

            if (friends.isEmpty()) {
                log.info("등록된 친구가 없습니다");
                return;
            }

            log.info("수집 대상: {} 명", friends.size());

            for (Summoner friend : friends) {
                try {
                    rankService.collectRankSnapshot(friend.getPuuid(), friend.getPlatform());
                } catch (RiotApiException e) {
                    log.error("{}의 랭크 수집 실패 (puuid: {})", friend.getGameName(), friend.getPuuid(), e);
                } catch (Exception e) {
                    log.error("예상 밖의 오류 발생 (puuid: {})", friend.getPuuid(), e);
                }
            }

            log.info("========== 랭크 스냅샷 수집 완료 ==========");
        } finally {
            isCollecting.set(false);
        }
    }
}