package com.happy.VenueService.dto;

import com.happy.VenueService.entity.Venue;
import com.happy.VenueService.entity.VenueStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "VenueResponse", description = "Venue detail response")
public class VenueResponse {
    @Schema(description = "Venue UUID")
    private UUID id;
    @Schema(description = "Host UUID")
    private UUID hostId;
    @Schema(description = "Venue name")
    private String name;
    @Schema(description = "Venue address")
    private String address;
    @Schema(description = "City code")
    private String cityCode;
    @Schema(description = "Latitude")
    private Double latitude;
    @Schema(description = "Longitude")
    private Double longitude;
    @Schema(description = "Venue description")
    private String description;
    @Schema(description = "Total capacity")
    private Integer capacity;
    @Schema(description = "Event start time in ISO-8601 with offset")
    private OffsetDateTime startTime;
    @Schema(description = "Event end time in ISO-8601 with offset")
    private OffsetDateTime endTime;
    @Schema(description = "Poster URL")
    private String posterUrl;
    @Schema(description = "Venue status")
    private VenueStatus status;
    @Schema(description = "Record created time in ISO-8601 with offset")
    private OffsetDateTime createdAt;
    @Schema(description = "Record updated time in ISO-8601 with offset")
    private OffsetDateTime updatedAt;
    @Schema(description = "Ticket tiers")
    private List<TicketTierResponse> ticketTiers = new ArrayList<>();

    public static VenueResponse toResponse(Venue venue) {
        return VenueResponse.builder()
                .id(venue.getId())
                .hostId(venue.getHostId())
                .name(venue.getName())
                .address(venue.getAddress())
                .cityCode(venue.getCityCode())
                .latitude(venue.getLatitude())
                .longitude(venue.getLongitude())
                .description(venue.getDescription())
                .capacity(venue.getCapacity())
                .startTime(venue.getStartTime())
                .endTime(venue.getEndTime())
                .posterUrl(venue.getPosterUrl())
                .status(venue.getStatus())
                .createdAt(venue.getCreatedAt())
                .updatedAt(venue.getUpdatedAt())
                .ticketTiers(venue.getTicketTiers().stream()
                        .map(tier -> TicketTierResponse.builder()
                                .id(tier.getId())
                                .tierName(tier.getTierName())
                                .price(tier.getPrice())
                                .totalCapacity(tier.getTotalCapacity())
                            .saleStartTime(tier.getSaleStartTime())
                            .saleEndTime(tier.getSaleEndTime())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
