package com.happy.TicketingService.common.response;

import io.swagger.v3.oas.annotations.media.Schema;

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
