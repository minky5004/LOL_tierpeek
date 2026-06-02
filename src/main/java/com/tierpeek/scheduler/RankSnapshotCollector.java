package com.tierpeek.scheduler;

import com.tierpeek.entity.Summoner;
import com.tierpeek.exception.RiotApiException;
import com.tierpeek.service.FriendService;
import com.tierpeek.service.RankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankSnapshotCollector {

    private final FriendService friendService;
    private final RankService rankService;

    @Scheduled(fixedRate = 1800000) // 30분마다 (1800000ms)
    public void collectRankSnapshots() {
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
    }
}