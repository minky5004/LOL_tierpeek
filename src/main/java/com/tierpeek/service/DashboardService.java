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

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FriendService friendService;
    private final RankService rankService;
    private final MatchService matchService;

    private static final int RECENT_MATCHES_COUNT = 5;
    private static final String SOLO_RANK_QUEUE = "RANKED_SOLO_5x5";

    @Transactional(readOnly = true)
    public List<FriendCardDto> getDashboard() {
        log.info("대시보드 조회 시작");

        List<Summoner> friends = friendService.getAllFriends();

        List<FriendCardDto> cards = friends.stream()
                .map(this::buildFriendCard)
                .collect(Collectors.toList());

        log.info("대시보드 조회 완료: {} 명의 친구", cards.size());
        return cards;
    }

    private FriendCardDto buildFriendCard(Summoner friend) {
        Optional<RankSnapshot> latestRank = rankService.getLatestRank(friend.getPuuid(), SOLO_RANK_QUEUE);

        Pageable pageable = PageRequest.of(0, RECENT_MATCHES_COUNT);
        List<MatchRecord> recentMatches = matchService.getRecentMatches(friend.getPuuid(), pageable);

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

        if (latestRank.isPresent()) {
            RankSnapshot rank = latestRank.get();
            builder.tier(rank.getTier())
                    .division(rank.getDivision())
                    .leaguePoints(rank.getLeaguePoints())
                    .wins(rank.getWins())
                    .losses(rank.getLosses());
        }

        return builder.build();
    }
}