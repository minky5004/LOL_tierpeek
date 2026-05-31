package com.loltracker.dto.riot;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class LeagueEntryDto {
    @JsonProperty("summonerId")
    private String summonerId;

    @JsonProperty("puuid")
    private String puuid;

    @JsonProperty("queueType")
    private String queueType;

    @JsonProperty("tier")
    private String tier;

    @JsonProperty("rank")
    private String rank;

    @JsonProperty("leaguePoints")
    private int leaguePoints;

    @JsonProperty("wins")
    private int wins;

    @JsonProperty("losses")
    private int losses;

    @JsonProperty("hotStreak")
    private boolean hotStreak;

    @JsonProperty("veteran")
    private boolean veteran;

    @JsonProperty("freshBlood")
    private boolean freshBlood;

    @JsonProperty("inactive")
    private boolean inactive;
}
