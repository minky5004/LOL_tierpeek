package com.loltracker.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
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
    public static class MatchSummaryDto {
        private String matchId;
        private String champion;
        private boolean win;
        private int kills;
        private int deaths;
        private int assists;
    }
}
