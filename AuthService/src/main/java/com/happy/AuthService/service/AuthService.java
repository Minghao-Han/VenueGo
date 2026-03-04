package com.happy.AuthService.service;

import com.happy.AuthService.common.response.ApiResponse;
import com.happy.AuthService.dto.AuthResponse;
import com.happy.AuthService.dto.LoginRequest;
import com.happy.AuthService.dto.RegisterRequest;

public interface AuthService {

    ApiResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    ApiResponse logout(String authorizationHeader);
}
