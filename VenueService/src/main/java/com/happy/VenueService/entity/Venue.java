package com.happy.VenueService.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.happy.VenueService.util.UUID.IdGeneratorProvider;


/*
Users commonly query nearby events in VenueGo.
This model keeps a composite index on latitude/longitude and an index on city_code.
In practice, city filtering is applied before distance calculations to reduce query cost.
*/
@Data
@NoArgsConstructor
@Entity
@Table(name = "venues", indexes = {
    @Index(name = "idx_venue_host", columnList = "host_id"),
    @Index(name = "idx_venue_city", columnList = "city_code"),
    @Index(name = "idx_venue_coords", columnList = "latitude, longitude"),
    @Index(name = "idx_start_time", columnList = "start_time") // Add time index to improve sorting.
})
public class Venue {

    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(columnDefinition = "BINARY(16)") // Store UUID as BINARY(16) for MySQL performance.
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private String address;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "host_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID hostId;

    @Column(name = "city_code", length = 20)
    private String cityCode;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "capacity")
    private Integer capacity;

    // Event-specific properties.
    
    @Column(name = "start_time")
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    @Column(name = "poster_url")
    private String posterUrl; // Poster image URL.

    @Enumerated(EnumType.STRING)
    private VenueStatus status = VenueStatus.UPCOMING;

    /**
        * Ticket tiers are stored with a one-to-many relation.
        * orphanRemoval allows direct tier add/delete in management flows.
     */
    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<TicketTier> ticketTiers = new ArrayList<>();

        // Audit fields.

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        // Generate UUID according to the configured strategy
        if (this.id == null) {
            this.id = IdGeneratorProvider.generateId();
        }
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void addTicketTier(TicketTier tier) {
        if (tier == null) {
            return;
        }
        this.ticketTiers.add(tier);
        tier.setVenue(this);
    }

    public void clearTicketTiers() {
        for (TicketTier tier : this.ticketTiers) {
            tier.setVenue(null);
        }
        this.ticketTiers.clear();
    }
}
