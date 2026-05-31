package com.tierpeek.controller;

import com.tierpeek.dto.ApiResponse;
import com.tierpeek.dto.request.AddFriendRequest;
import com.tierpeek.entity.Summoner;
import com.tierpeek.service.FriendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @PostMapping
    public ResponseEntity<ApiResponse<Summoner>> addFriend(@Valid @RequestBody AddFriendRequest request) {
        log.info("친구 추가 요청: {}#{}", request.getGameName(), request.getTagLine());
        Summoner summoner = friendService.addFriend(
                request.getGameName(),
                request.getTagLine(),
                request.getPlatform()
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("친구가 추가되었습니다", summoner));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Summoner>>> getAllFriends() {
        List<Summoner> friends = friendService.getAllFriends();
        return ResponseEntity.ok(ApiResponse.success(friends));
    }

    @GetMapping("/{puuid}")
    public ResponseEntity<ApiResponse<Summoner>> getFriend(@PathVariable String puuid) {
        Summoner summoner = friendService.getFriendByPuuid(puuid);
        return ResponseEntity.ok(ApiResponse.success(summoner));
    }

    @DeleteMapping("/{puuid}")
    public ResponseEntity<ApiResponse<Void>> removeFriend(@PathVariable String puuid) {
        friendService.removeFriend(puuid);
        return ResponseEntity.ok(ApiResponse.success("친구가 제거되었습니다", null));
    }
}
