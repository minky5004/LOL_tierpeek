package com.tierpeek.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FriendCardDto {
    private String puuid;
    private String gameName;
    private String tagLine;
    private String tier;
    private String division;
    private int leaguePoints;
    private int wins;
    private int losses;
    private List<MatchSummaryDto> recentMatches;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchSummaryDto {
        private String matchId;
        private String champion;
        private boolean win;
        private int kills;
        private int deaths;
        private int assists;
    }
}
