package com.loltracker.util;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public class RiotRateLimiter {

    private static final int REQUESTS_PER_SECOND = 20;
    private static final int REQUESTS_PER_TWO_MINUTES = 100;
    private static final long TWO_MINUTES_IN_MS = 120_000L;

    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public boolean allowRequest(String region) {
        TokenBucket bucket = buckets.computeIfAbsent(region, k -> new TokenBucket());
        return bucket.tryConsume();
    }

    public void waitIfNeeded(String region) {
        TokenBucket bucket = buckets.computeIfAbsent(region, k -> new TokenBucket());
        bucket.waitIfNeeded();
    }

    private static class TokenBucket {
        private volatile long lastRefillTimeSecond = System.currentTimeMillis();
        private volatile long lastRefillTime2Min = System.currentTimeMillis();
        private volatile int tokensSecond = REQUESTS_PER_SECOND;
        private volatile int tokens2Min = REQUESTS_PER_TWO_MINUTES;

        synchronized boolean tryConsume() {
            refill();
            if (tokensSecond > 0 && tokens2Min > 0) {
                tokensSecond--;
                tokens2Min--;
                return true;
            }
            return false;
        }

        synchronized void waitIfNeeded() {
            while (!tryConsume()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Rate limit 대기 중 중단됨", e);
                }
            }
        }

        private void refill() {
            long now = System.currentTimeMillis();

            // 초당 한도 리필
            if (now - lastRefillTimeSecond >= 1000L) {
                tokensSecond = REQUESTS_PER_SECOND;
                lastRefillTimeSecond = now;
            }

            // 2분 한도 리필
            if (now - lastRefillTime2Min >= TWO_MINUTES_IN_MS) {
                tokens2Min = REQUESTS_PER_TWO_MINUTES;
                lastRefillTime2Min = now;
            }
        }
    }
}
