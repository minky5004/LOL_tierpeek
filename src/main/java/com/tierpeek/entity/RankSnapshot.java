package com.tierpeek.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "rank_snapshot", indexes = {
        @Index(name = "idx_snapshot_query", columnList = "puuid, queue_type, recorded_at DESC")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String puuid;

    @Column(nullable = false, length = 30)
    private String queueType;

    @Column(nullable = false, length = 20)
    private String tier;

    @Column(length = 5)
    private String division;

    @Column(nullable = false)
    private Integer leaguePoints;

    @Column(nullable = false)
    private Integer wins;

    @Column(nullable = false)
    private Integer losses;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onCreate() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
    }
}
