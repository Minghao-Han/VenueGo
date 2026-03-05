package com.happy.UserService.service.impl;

import com.happy.UserService.dto.UpdateProfileRequest;
import com.happy.UserService.dto.UserProfileDTO;
import com.happy.UserService.entity.UserProfile;
import com.happy.UserService.exception.BusinessException;
import com.happy.UserService.repository.UserProfileRepository;
import com.happy.UserService.service.UserProfileService;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository profileRepository;

    @Override
    public UserProfileDTO getProfile(UUID userId) {
        UserProfile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        return toDTO(profile);
    }

    @Override
    public UserProfileDTO updateProfile(UUID userId, UpdateProfileRequest request) {
        UserProfile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        if (request.getUsername() != null) profile.setUsername(request.getUsername());
        if (request.getAvatarUrl() != null) profile.setAvatarUrl(request.getAvatarUrl());
        if (request.getBio() != null) profile.setBio(request.getBio());

        return toDTO(profileRepository.save(profile));
    }

    @Override
    public void createProfile(UUID userId, String username, String email) {
        if (profileRepository.existsById(userId)) return; // 幂等，防止重复创建
        UserProfile profile = UserProfile.builder()
                .id(userId)
                .username(username)
                .email(email)
                .build();
        profileRepository.save(profile);
    }

    private UserProfileDTO toDTO(UserProfile p) {
        return UserProfileDTO.builder()
                .id(p.getId())
                .username(p.getUsername())
                .email(p.getEmail())
                .avatarUrl(p.getAvatarUrl())
                .bio(p.getBio())
                .points(p.getPoints())
                .build();
    }
}