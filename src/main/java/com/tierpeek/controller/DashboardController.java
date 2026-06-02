package com.tierpeek.controller;

import com.tierpeek.dto.ApiResponse;
import com.tierpeek.dto.FriendCardDto;
import com.tierpeek.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 대시보드 API 엔드포인트입니다.
 * 등록된 친구들의 현재 랭크와 최근 전적을 한 화면에서 조회할 수 있습니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 등록된 친구들의 대시보드 정보를 조회합니다.
     * 각 친구의 현재 솔로 랭크와 최근 5경기 정보를 포함합니다.
     *
     * @return 친구별 카드 정보 리스트
     */
    @GetMapping
    public ApiResponse<List<FriendCardDto>> getDashboard() {
        log.info("대시보드 API 호출");
        List<FriendCardDto> dashboard = dashboardService.getDashboard();
        return ApiResponse.success("대시보드 조회 성공", dashboard);
    }
}
