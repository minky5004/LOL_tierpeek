package com.loltracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "summoner")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Summoner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String puuid;

    @Column(nullable = false, length = 100)
    private String gameName;

    @Column(nullable = false, length = 20)
    private String tagLine;

    @Column(nullable = false, length = 10)
    private String platform;

    @Column
    private Integer profileIconId;

    @Column
    private Integer summonerLevel;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
