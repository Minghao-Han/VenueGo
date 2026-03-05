package com.happy.VenueService.dto;

import com.happy.VenueService.entity.VenueStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(name = "VenueUpsertRequest", description = "Request payload for creating or updating a venue")
public class VenueUpsertRequest {
    @Schema(description = "Venue name", example = "Jay Chou World Tour")
    private String name;
    @Schema(description = "Venue address", example = "Shanghai Stadium, Xuhui District")
    private String address;
    @Schema(description = "City code", example = "SH")
    private String cityCode;
    @Schema(description = "Latitude", example = "31.188")
    private Double latitude;
    @Schema(description = "Longitude", example = "121.437")
    private Double longitude;
    @Schema(description = "Venue description", example = "Large indoor venue suitable for concerts")
    private String description;
    @Schema(description = "Total capacity", example = "50000")
    private Integer capacity;
    @Schema(description = "Event start time in ISO-8601 with offset", example = "2026-05-01T19:30:00+08:00")
    private OffsetDateTime startTime;
    @Schema(description = "Event end time in ISO-8601 with offset", example = "2026-05-01T22:00:00+08:00")
    private OffsetDateTime endTime;
    @Schema(description = "Poster image URL", example = "https://cdn.example.com/posters/venue-1.jpg")
    private String posterUrl;
    @Schema(description = "Venue status")
    private VenueStatus status;
    @Schema(description = "Ticket tiers")
    private List<TicketTierRequest> ticketTiers = new ArrayList<>();
}
