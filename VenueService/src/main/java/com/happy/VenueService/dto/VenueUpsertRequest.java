package com.happy.VenueService.dto;

import com.happy.VenueService.entity.VenueStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VenueUpsertRequest {
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
    private List<TicketTierRequest> ticketTiers = new ArrayList<>();
}
