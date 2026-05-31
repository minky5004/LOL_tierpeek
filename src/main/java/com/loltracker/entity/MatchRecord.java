package com.loltracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_record",
       uniqueConstraints = @UniqueConstraint(columnNames = {"match_id", "puuid"}),
       indexes = {
           @Index(name = "idx_match_query", columnList = "puuid, game_creation DESC")
       })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String matchId;

    @Column(nullable = false, length = 100)
    private String puuid;

    @Column(length = 40)
    private String champion;

    @Column
    private Boolean win;

    @Column
    private Integer kills;

    @Column
    private Integer deaths;

    @Column
    private Integer assists;

    @Column
    private Integer queueId;

    @Column
    private LocalDateTime gameCreation;

    @Column
    private Integer gameDuration;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
