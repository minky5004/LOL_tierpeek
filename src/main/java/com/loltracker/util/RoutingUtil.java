package com.loltracker.util;

public class RoutingUtil {

    private RoutingUtil() {
    }

    public static String getRegionalUrl(String platform) {
        return "https://asia.api.riotgames.com";
    }

    public static String getPlatformUrl(String platform) {
        return switch (platform.toLowerCase()) {
            case "kr" -> "https://kr.api.riotgames.com";
            case "na" -> "https://na1.api.riotgames.com";
            case "euw" -> "https://euw1.api.riotgames.com";
            case "eune" -> "https://eun1.api.riotgames.com";
            default -> throw new IllegalArgumentException("지원하지 않는 플랫폼: " + platform);
        };
    }

    public static boolean isRegionalEndpoint(String endpoint) {
        return endpoint.contains("account-v1") || endpoint.contains("match-v5");
    }

    public static boolean isPlatformEndpoint(String endpoint) {
        return endpoint.contains("league-v4") || endpoint.contains("summoner-v4");
    }
}
