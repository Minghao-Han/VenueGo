package com.happy.AuthService.common.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Shared API response payload for simple message-based responses.
 * This class is placed in a common package so it can be reused by future services.
 */
@Schema(name = "ApiResponse", description = "Generic message response")
public class ApiResponse {

    @Schema(description = "Human-readable result message", example = "Register success")
    private final String message;

    public ApiResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
