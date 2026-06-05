package com.tierpeek.scheduler;

import com.tierpeek.config.CacheConfig;
import com.tierpeek.entity.Summoner;
import com.tierpeek.exception.RiotApiException;
import com.tierpeek.service.FriendService;
import com.tierpeek.service.MatchService;
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
public class MatchSyncJob {

    private final FriendService friendService;
    private final MatchService matchService;

    private static final int MATCH_COUNT = 20;

    @Value("${match.sync.fixedRateMs:1800000}")
    private long fixedRateMs;

    private final AtomicBoolean isSyncing = new AtomicBoolean(false);

    /**
     * 등록된 친구들의 최신 매치 기록을 주기적으로 동기화합니다.
     * 동기화 후 관련 캐시를 초기화합니다.
     */
    @CacheEvict(value = {CacheConfig.DASHBOARD_CACHE, CacheConfig.MATCHES_CACHE}, allEntries = true)
    @Scheduled(fixedRateString = "${match.sync.fixedRateMs}")
    public void syncMatches() {
        // 동시 실행 방지
        if (!isSyncing.compareAndSet(false, true)) {
            log.warn("매치 동기화가 이미 진행 중입니다. 건너뜀");
            return;
        }

        try {
            log.info("========== 매치 동기화 시작 ==========");

            List<Summoner> friends = friendService.getAllFriends();

            if (friends.isEmpty()) {
                log.info("등록된 친구가 없습니다");
                return;
            }

            log.info("동기화 대상: {} 명", friends.size());

            for (Summoner friend : friends) {
                try {
                    matchService.syncMatches(friend.getPuuid(), MATCH_COUNT);
                } catch (RiotApiException e) {
                    log.error("{}의 매치 동기화 실패 (puuid: {})", friend.getGameName(), friend.getPuuid(), e);
                } catch (Exception e) {
                    log.error("예상 밖의 오류 발생 (puuid: {})", friend.getPuuid(), e);
                }
            }

            log.info("========== 매치 동기화 완료 ==========");
        } finally {
            isSyncing.set(false);
        }
    }
}
