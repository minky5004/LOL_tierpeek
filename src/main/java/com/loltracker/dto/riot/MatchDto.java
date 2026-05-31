package com.loltracker.dto.riot;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class MatchDto {
    @JsonProperty("metadata")
    private MetadataDto metadata;

    @JsonProperty("info")
    private InfoDto info;

    @Getter
    public static class MetadataDto {
        @JsonProperty("matchId")
        private String matchId;

        @JsonProperty("dataVersion")
        private String dataVersion;

        @JsonProperty("participants")
        private List<String> participants;
    }

    @Getter
    public static class InfoDto {
        @JsonProperty("gameId")
        private long gameId;

        @JsonProperty("queueId")
        private int queueId;

        @JsonProperty("gameCreation")
        private long gameCreation;

        @JsonProperty("gameDuration")
        private long gameDuration;

        @JsonProperty("participants")
        private List<ParticipantDto> participants;
    }

    @Getter
    public static class ParticipantDto {
        @JsonProperty("puuid")
        private String puuid;

        @JsonProperty("championName")
        private String championName;

        @JsonProperty("championId")
        private int championId;

        @JsonProperty("win")
        private boolean win;

        @JsonProperty("kills")
        private int kills;

        @JsonProperty("deaths")
        private int deaths;

        @JsonProperty("assists")
        private int assists;

        @JsonProperty("role")
        private String role;
    }
}
