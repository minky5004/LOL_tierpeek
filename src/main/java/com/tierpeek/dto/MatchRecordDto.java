package com.tierpeek.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatchRecordDto {
    private String matchId;
    private String champion;
    private boolean win;
    private int kills;
    private int deaths;
    private int assists;
    private int queueId;
    private LocalDateTime gameCreation;
    private int gameDuration;
}