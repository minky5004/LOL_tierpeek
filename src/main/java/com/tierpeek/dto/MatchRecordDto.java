package com.tierpeek.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatchRecordDto {
    /** 매치 고유 식별자 */
    private String matchId;

    /** 플레이한 챔피언명 */
    private String champion;

    /** 승패 여부 */
    private boolean win;

    /** 킬 수 */
    private int kills;

    /** 사망 수 */
    private int deaths;

    /** 어시스트 수 */
    private int assists;

    /** 매치 큐 타입 ID */
    private int queueId;

    /** 게임 생성 시간 */
    private LocalDateTime gameCreation;

    /** 게임 진행 시간 (초) */
    private int gameDuration;
}