package com.happy.VenueService.controller;

import com.happy.VenueService.dto.VenueResponse;
import com.happy.VenueService.dto.VenueUpsertRequest;
import com.happy.VenueService.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/venues")
@Tag(name = "Venue", description = "Venue management REST APIs")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create venue", description = "Create a new venue with ticket tier settings")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Venue created", content = @Content(schema = @Schema(implementation = VenueResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<VenueResponse> createVenue(
            @Parameter(description = "Host UUID from request header", required = true)
            @RequestHeader("host_id") UUID hostId,
            @Valid @RequestBody VenueUpsertRequest request) {
        return ResponseEntity.ok(venueService.createVenue(request, hostId));
    }

    @PutMapping("/{venueId}")
    @Operation(summary = "Update venue", description = "Update an existing venue by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Venue updated", content = @Content(schema = @Schema(implementation = VenueResponse.class))),
            @ApiResponse(responseCode = "404", description = "Venue not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<VenueResponse> updateVenue(
            @Parameter(description = "Venue UUID", required = true) @PathVariable UUID venueId,
            @Parameter(description = "Host UUID from request header", required = true)
            @RequestHeader("host_id") UUID hostId,
            @Valid @RequestBody VenueUpsertRequest request) {
        return ResponseEntity.ok(venueService.updateVenue(venueId, request, hostId));
    }

    @DeleteMapping("/{venueId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete venue", description = "Delete a venue by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Venue deleted"),
            @ApiResponse(responseCode = "404", description = "Venue not found")
    })
    public ResponseEntity<Void> deleteVenue(
            @Parameter(description = "Venue UUID", required = true) @PathVariable UUID venueId,
            @Parameter(description = "Host UUID from request header", required = true)
            @RequestHeader("host_id") UUID hostId) {
        venueService.deleteVenue(venueId, hostId);
        return ResponseEntity.ok().build();
    }
}
