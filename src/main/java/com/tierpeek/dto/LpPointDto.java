package com.tierpeek.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LpPointDto {
    private LocalDateTime recordedAt;
    private int leaguePoints;
    private String tier;
    private String division;
    private int wins;
    private int losses;
}
