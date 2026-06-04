package com.tierpeek.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tierpeek.dto.riot.AccountDto;
import com.tierpeek.dto.riot.LeagueEntryDto;
import com.tierpeek.dto.riot.MatchDto;
import com.tierpeek.exception.RiotApiException;
import com.tierpeek.util.RiotRateLimiter;
import com.tierpeek.util.RoutingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RiotApiClient {

    private final WebClient.Builder webClientBuilder;
    private final RiotRateLimiter rateLimiter;
    private final ObjectMapper objectMapper;

    /**
     * Riot ID로 계정 정보를 조회합니다.
     *
     * @param gameName 게임명
     * @param tagLine 태그
     * @param platform 플랫폼 (kr, na, euw 등)
     * @return 계정 정보 DTO
     * @throws RiotApiException API 호출 실패 시
     */
    public AccountDto getAccountByRiotId(String gameName, String tagLine, String platform) {
        rateLimiter.waitIfNeeded(platform);

        try {
            String url = RoutingUtil.getRegionalUrl(platform);
            return webClientBuilder.baseUrl(url).build()
                    .get()
                    .uri("/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}", gameName, tagLine)
                    .retrieve()
                    .bodyToMono(AccountDto.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                            .maxBackoff(Duration.ofSeconds(10))
                            .filter(this::isRetryable))
                    .block();
        } catch (Exception e) {
            log.error("Riot ID 조회 실패: {}/{}", gameName, tagLine, e);
            throw new RiotApiException("계정을 찾을 수 없습니다: " + gameName + "#" + tagLine, 404, e);
        }
    }

    /**
     * 소환사의 리그 엔트리(랭크 정보)를 조회합니다.
     *
     * @param puuid 소환사 고유 식별자
     * @param platform 플랫폼 (kr, na, euw 등)
     * @return 리그 엔트리 목록 (솔로, 자유, 3v3 등)
     * @throws RiotApiException API 호출 실패 시
     */
    public List<LeagueEntryDto> getLeagueEntriesByPuuid(String puuid, String platform) {
        rateLimiter.waitIfNeeded(platform);

        try {
            String url = RoutingUtil.getPlatformUrl(platform);
            return webClientBuilder.baseUrl(url).build()
                    .get()
                    .uri("/lol/league/v4/entries/by-puuid/{puuid}", puuid)
                    .retrieve()
                    .bodyToFlux(LeagueEntryDto.class)
                    .collectList()
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                            .maxBackoff(Duration.ofSeconds(10))
                            .filter(this::isRetryable))
                    .block();
        } catch (Exception e) {
            log.error("리그 엔트리 조회 실패: {}", puuid, e);
            throw new RiotApiException("랭크 정보를 조회할 수 없습니다", 500, e);
        }
    }

    /**
     * 소환사의 매치 ID 목록을 조회합니다.
     *
     * @param puuid 소환사 고유 식별자
     * @param platform 플랫폼 (kr, na, euw 등)
     * @param start 조회 시작 인덱스
     * @param count 조회할 매치 개수 (최대 100)
     * @return 매치 ID 목록
     * @throws RiotApiException API 호출 실패 시
     */
    public List<String> getMatchIdsByPuuid(String puuid, String platform, int start, int count) {
        rateLimiter.waitIfNeeded(platform);

        try {
            String url = RoutingUtil.getRegionalUrl(platform);
            String jsonResponse = webClientBuilder.baseUrl(url).build()
                    .get()
                    .uri("/lol/match/v5/matches/by-puuid/{puuid}/ids?start={start}&count={count}",
                            puuid, start, count)
                    .retrieve()
                    .bodyToMono(String.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                            .maxBackoff(Duration.ofSeconds(10))
                            .filter(this::isRetryable))
                    .block();

            return Arrays.asList(objectMapper.readValue(jsonResponse, String[].class));
        } catch (Exception e) {
            log.error("매치 ID 조회 실패: {}", puuid, e);
            throw new RiotApiException("매치 기록을 조회할 수 없습니다", 500, e);
        }
    }

    /**
     * 특정 매치의 상세 정보를 조회합니다.
     *
     * @param matchId 매치 고유 식별자
     * @param platform 플랫폼 (kr, na, euw 등)
     * @return 매치 상세 정보 DTO
     * @throws RiotApiException API 호출 실패 시
     */
    public MatchDto getMatchDetail(String matchId, String platform) {
        rateLimiter.waitIfNeeded(platform);

        try {
            String url = RoutingUtil.getRegionalUrl(platform);
            return webClientBuilder.baseUrl(url).build()
                    .get()
                    .uri("/lol/match/v5/matches/{matchId}", matchId)
                    .retrieve()
                    .bodyToMono(MatchDto.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                            .maxBackoff(Duration.ofSeconds(10))
                            .filter(this::isRetryable))
                    .block();
        } catch (Exception e) {
            log.error("매치 상세 조회 실패: {}", matchId, e);
            throw new RiotApiException("매치 상세 정보를 조회할 수 없습니다", 500, e);
        }
    }

    /**
     * 재시도 가능한 에러 여부를 판단합니다.
     * 429(Too Many Requests), 503(Service Unavailable) 상태 코드는 재시도합니다.
     *
     * @param throwable 발생한 예외
     * @return 재시도 가능 여부
     */
    private boolean isRetryable(Throwable throwable) {
        return throwable.getMessage() != null &&
               (throwable.getMessage().contains("429") ||
                throwable.getMessage().contains("503"));
    }
}
