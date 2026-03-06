package com.happy.TicketingService.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "ticket_inventories")
public class TicketInventory {

    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "ticket_tier_id", nullable = false, unique = true, columnDefinition = "BINARY(16)")
    private UUID ticketTierId;

    @Column(nullable = false)
    private Integer availableInventory;

    @Column(name = "sale_start_time")
    private OffsetDateTime saleStartTime;

    @Column(name = "sale_end_time")
    private OffsetDateTime saleEndTime;

    @Column(nullable = false)
    private Integer purchaseLimit;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }
}
