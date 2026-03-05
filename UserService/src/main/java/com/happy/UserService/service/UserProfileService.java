package com.happy.UserService.service;

import java.util.UUID;

import com.happy.UserService.dto.UpdateProfileRequest;
import com.happy.UserService.dto.UserProfileDTO;

public interface UserProfileService {
    UserProfileDTO getProfile(UUID userId);
    UserProfileDTO updateProfile(UUID userId, UpdateProfileRequest request);
    void createProfile(UUID userId, String username, String email);
}