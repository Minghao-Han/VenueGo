package com.happy.VenueService.dto;

import com.happy.VenueService.entity.VenueStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueResponse {
    private UUID id;
    private String name;
    private String address;
    private String cityCode;
    private Double latitude;
    private Double longitude;
    private String description;
    private Integer capacity;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String posterUrl;
    private VenueStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TicketTierResponse> ticketTiers = new ArrayList<>();
}
