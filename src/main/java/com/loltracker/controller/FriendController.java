package com.loltracker.controller;

import com.loltracker.dto.ApiResponse;
import com.loltracker.dto.request.AddFriendRequest;
import com.loltracker.entity.Summoner;
import com.loltracker.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Friends", description = "친구 관리 API")
public class FriendController {

    private final FriendService friendService;

    @PostMapping
    @Operation(summary = "친구 추가", description = "Riot ID로 친구를 등록합니다")
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
    @Operation(summary = "친구 목록 조회", description = "등록된 모든 친구를 조회합니다")
    public ResponseEntity<ApiResponse<List<Summoner>>> getAllFriends() {
        List<Summoner> friends = friendService.getAllFriends();
        return ResponseEntity.ok(ApiResponse.success(friends));
    }

    @GetMapping("/{puuid}")
    @Operation(summary = "친구 조회", description = "특정 친구의 정보를 조회합니다")
    public ResponseEntity<ApiResponse<Summoner>> getFriend(@PathVariable String puuid) {
        Summoner summoner = friendService.getFriendByPuuid(puuid);
        return ResponseEntity.ok(ApiResponse.success(summoner));
    }

    @DeleteMapping("/{puuid}")
    @Operation(summary = "친구 제거", description = "등록된 친구를 제거합니다")
    public ResponseEntity<ApiResponse<Void>> removeFriend(@PathVariable String puuid) {
        friendService.removeFriend(puuid);
        return ResponseEntity.ok(ApiResponse.success("친구가 제거되었습니다", null));
    }
}
