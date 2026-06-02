package com.tierpeek.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 매치 기록 정보를 나타내는 DTO입니다.
 * null 필드는 JSON 직렬화에서 제외됩니다.
 */
@Getter
@Builder
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