package com.happy.UserService.controller;

import com.happy.UserService.common.ApiResponse;
import com.happy.UserService.service.DailySignService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user/daily-sign")
@RequiredArgsConstructor
public class DailySignController {

    private final DailySignService dailySignService;

    // 签到
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> sign(
            @RequestHeader("X-User-Id") String userId) {
        dailySignService.sign(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 连续签到天数
    @GetMapping("/streak")
    public ResponseEntity<ApiResponse<Integer>> getStreak(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(ApiResponse.success(dailySignService.getStreak(UUID.fromString(userId))));
    }

    // 本月签到记录
    @GetMapping("/month")
    public ResponseEntity<ApiResponse<List<Integer>>> getMonthRecord(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(ApiResponse.success(dailySignService.getMonthRecord(UUID.fromString(userId))));
    }
}