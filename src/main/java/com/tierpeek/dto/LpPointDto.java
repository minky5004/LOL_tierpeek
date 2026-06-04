package com.tierpeek.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LpPointDto {
    private LocalDateTime recordedAt;
    private int leaguePoints;
    private String tier;
    private String division;
    private int wins;
    private int losses;
}
