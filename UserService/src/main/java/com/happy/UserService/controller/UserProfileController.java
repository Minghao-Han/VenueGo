package com.happy.UserService.controller;

import com.happy.UserService.common.ApiResponse;
import com.happy.UserService.dto.UpdateProfileRequest;
import com.happy.UserService.dto.UserProfileDTO;
import com.happy.UserService.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/user/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserProfileDTO>> getProfile(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(ApiResponse.success(userProfileService.getProfile(UUID.fromString(userId))));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateProfile(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userProfileService.updateProfile(UUID.fromString(userId), request)));
    }
}
