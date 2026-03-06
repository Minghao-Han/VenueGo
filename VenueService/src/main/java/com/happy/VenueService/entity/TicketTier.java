package com.happy.VenueService.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "ticket_tiers")
public class TicketTier {

    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(columnDefinition = "BINARY(16)") // Store UUID as BINARY(16) for MySQL performance.
    private UUID id;

    @Column(nullable = false)
    private String tierName; // Examples: VIP, Early Bird, Regular.

    @Column(nullable = false)
    private BigDecimal price; // BigDecimal is preferred for money values.

    @Column(nullable = false)
    private Integer totalCapacity; // Total inventory capacity.

    @Column(nullable = false)
    private Integer purchaseLimit; // Per-user purchase limit.

    @Column(name = "sale_start_time")
    private OffsetDateTime saleStartTime;

    @Column(name = "sale_end_time")
    private OffsetDateTime saleEndTime;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }
}