package com.tierpeek.scheduler;

import com.tierpeek.entity.Summoner;
import com.tierpeek.exception.RiotApiException;
import com.tierpeek.service.FriendService;
import com.tierpeek.service.MatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchSyncJob {

    private final FriendService friendService;
    private final MatchService matchService;

    private static final int MATCH_COUNT = 20;

    @Scheduled(fixedRate = 3600000) // 60분마다 (3600000ms)
    public void syncMatches() {
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
    }
}
