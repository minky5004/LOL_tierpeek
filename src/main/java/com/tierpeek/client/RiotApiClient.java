package com.tierpeek.client;

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
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RiotApiClient {

    private final WebClient riotWebClient;
    private final RiotRateLimiter rateLimiter;

    public AccountDto getAccountByRiotId(String gameName, String tagLine) {
        String region = "asia";
        rateLimiter.waitIfNeeded(region);

        try {
            return riotWebClient.get()
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

    public List<LeagueEntryDto> getLeagueEntriesByPuuid(String puuid, String platform) {
        rateLimiter.waitIfNeeded(platform);

        try {
            return riotWebClient.get()
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

    public List<String> getMatchIdsByPuuid(String puuid, int start, int count) {
        String region = "asia";
        rateLimiter.waitIfNeeded(region);

        try {
            return riotWebClient.get()
                    .uri("/lol/match/v5/matches/by-puuid/{puuid}/ids?start={start}&count={count}",
                            puuid, start, count)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .collectList()
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                            .maxBackoff(Duration.ofSeconds(10))
                            .filter(this::isRetryable))
                    .block();
        } catch (Exception e) {
            log.error("매치 ID 조회 실패: {}", puuid, e);
            throw new RiotApiException("매치 기록을 조회할 수 없습니다", 500, e);
        }
    }

    public MatchDto getMatchDetail(String matchId) {
        String region = "asia";
        rateLimiter.waitIfNeeded(region);

        try {
            return riotWebClient.get()
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

    private boolean isRetryable(Throwable throwable) {
        return throwable.getMessage() != null &&
               (throwable.getMessage().contains("429") ||
                throwable.getMessage().contains("503"));
    }
}
