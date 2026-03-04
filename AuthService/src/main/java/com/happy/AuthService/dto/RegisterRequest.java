package com.happy.AuthService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "RegisterRequest", description = "Payload to create a new user account")
public class RegisterRequest {

    @Schema(description = "Unique username", example = "alice", minLength = 3, maxLength = 50)
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @Schema(description = "Raw password to be hashed server-side", example = "StrongPass123", minLength = 6, maxLength = 100)
    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    @Schema(description = "Unique email address", example = "alice@example.com", maxLength = 100)
    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
