package com.tierpeek.service;

import com.tierpeek.dto.FriendCardDto;
import com.tierpeek.entity.MatchRecord;
import com.tierpeek.entity.RankSnapshot;
import com.tierpeek.entity.Summoner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 대시보드 정보 조회 서비스입니다.
 * 등록된 친구들의 현재 랭크와 최근 전적을 조합하여 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FriendService friendService;
    private final RankService rankService;
    private final MatchService matchService;

    private static final int RECENT_MATCHES_COUNT = 5;
    private static final String SOLO_RANK_QUEUE = "RANKED_SOLO_5x5";

    /**
     * 모든 친구의 대시보드 정보를 조회합니다.
     * 배치 조회로 N+1 쿼리를 방지합니다.
     *
     * @return 친구별 카드 정보 리스트
     */
    @Transactional(readOnly = true)
    public List<FriendCardDto> getDashboard() {
        log.info("대시보드 조회 시작");

        List<Summoner> friends = friendService.getAllFriends();

        if (friends.isEmpty()) {
            log.info("등록된 친구 없음");
            return List.of();
        }

        List<String> puuids = friends.stream()
                .map(Summoner::getPuuid)
                .collect(Collectors.toList());

        var ranksByPuuid = rankService.getLatestRanksForPuuids(puuids, SOLO_RANK_QUEUE);
        var matchesByPuuid = matchService.getRecentMatchesForPuuids(puuids, RECENT_MATCHES_COUNT);

        List<FriendCardDto> cards = friends.stream()
                .map(friend -> buildFriendCard(friend, ranksByPuuid, matchesByPuuid))
                .collect(Collectors.toList());

        log.info("대시보드 조회 완료: {} 명의 친구", cards.size());
        return cards;
    }

    /**
     * 친구 카드 정보를 구성합니다.
     *
     * @param friend 친구 정보
     * @param ranksByPuuid 랭크 정보 맵
     * @param matchesByPuuid 매치 정보 맵
     * @return 구성된 친구 카드 정보
     */
    private FriendCardDto buildFriendCard(Summoner friend,
                                          java.util.Map<String, RankSnapshot> ranksByPuuid,
                                          java.util.Map<String, List<MatchRecord>> matchesByPuuid) {
        List<MatchRecord> recentMatches = matchesByPuuid.getOrDefault(friend.getPuuid(), List.of());

        List<FriendCardDto.MatchSummaryDto> matchSummaries = recentMatches.stream()
                .map(m -> FriendCardDto.MatchSummaryDto.builder()
                        .matchId(m.getMatchId())
                        .champion(m.getChampion())
                        .win(m.getWin())
                        .kills(m.getKills())
                        .deaths(m.getDeaths())
                        .assists(m.getAssists())
                        .build())
                .collect(Collectors.toList());

        FriendCardDto.FriendCardDtoBuilder builder = FriendCardDto.builder()
                .puuid(friend.getPuuid())
                .gameName(friend.getGameName())
                .tagLine(friend.getTagLine())
                .recentMatches(matchSummaries);

        RankSnapshot latestRank = ranksByPuuid.get(friend.getPuuid());
        if (latestRank != null) {
            builder.tier(latestRank.getTier())
                    .division(latestRank.getDivision())
                    .leaguePoints(latestRank.getLeaguePoints())
                    .wins(latestRank.getWins())
                    .losses(latestRank.getLosses());
        }

        return builder.build();
    }
}