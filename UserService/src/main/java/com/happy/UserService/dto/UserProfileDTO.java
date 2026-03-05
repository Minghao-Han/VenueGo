package com.happy.UserService.dto;

import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private UUID id;
    private String username;
    private String email;
    private String avatarUrl;
    private String bio;
    private Integer points;
}
