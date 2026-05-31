package com.tierpeek.service;

import com.tierpeek.client.RiotApiClient;
import com.tierpeek.dto.riot.AccountDto;
import com.tierpeek.entity.Summoner;
import com.tierpeek.exception.FriendNotFoundException;
import com.tierpeek.repository.SummonerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendService {

    private final RiotApiClient riotApiClient;
    private final SummonerRepository summonerRepository;

    @Transactional
    public Summoner addFriend(String gameName, String tagLine, String platform) {
        log.info("친구 추가 시작: {}#{}", gameName, tagLine);

        summonerRepository.findByGameNameAndTagLine(gameName, tagLine)
                .ifPresent(s -> {
                    log.warn("이미 등록된 친구: {}#{}", gameName, tagLine);
                    throw new RuntimeException("이미 등록된 친구입니다");
                });

        AccountDto accountDto = riotApiClient.getAccountByRiotId(gameName, tagLine);

        Summoner summoner = Summoner.builder()
                .puuid(accountDto.getPuuid())
                .gameName(accountDto.getGameName())
                .tagLine(accountDto.getTagLine())
                .platform(platform != null ? platform : "kr")
                .build();

        Summoner saved = summonerRepository.save(summoner);
        log.info("친구 추가 완료: {} (puuid: {})", saved.getGameName(), saved.getPuuid());
        return saved;
    }

    @Transactional(readOnly = true)
    public Summoner getFriendByPuuid(String puuid) {
        return summonerRepository.findByPuuid(puuid)
                .orElseThrow(() -> new FriendNotFoundException("친구를 찾을 수 없습니다: " + puuid));
    }

    @Transactional(readOnly = true)
    public List<Summoner> getAllFriends() {
        return summonerRepository.findAll();
    }

    @Transactional
    public void removeFriend(String puuid) {
        Summoner summoner = getFriendByPuuid(puuid);
        summonerRepository.delete(summoner);
        log.info("친구 제거: {}", summoner.getGameName());
    }
}
