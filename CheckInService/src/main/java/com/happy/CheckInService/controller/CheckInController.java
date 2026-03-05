package com.happy.CheckInService.controller;

import com.happy.CheckInService.dto.CheckInResult;
import com.happy.CheckInService.service.CheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    /**
     * 扫码验票
     *
     * @param ticketCode 二维码解码内容
     */
    @PostMapping("/scan")
    public ResponseEntity<CheckInResult> scan(
            @RequestParam String ticketCode
    ) {
        CheckInResult result = checkInService.checkIn(ticketCode);
        return ResponseEntity.ok(result);
    }
}