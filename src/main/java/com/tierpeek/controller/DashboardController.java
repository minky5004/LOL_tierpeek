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

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ApiResponse<List<FriendCardDto>> getDashboard() {
        log.info("대시보드 API 호출");
        List<FriendCardDto> dashboard = dashboardService.getDashboard();
        return ApiResponse.success("대시보드 조회 성공", dashboard);
    }
}
