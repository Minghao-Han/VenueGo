package com.happy.VenueService.entity;

import java.math.BigDecimal;
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
@Table(name = "ticket_tiers")
public class TicketTier {

    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(columnDefinition = "BINARY(16)") // UUID 在 MySQL 中存为 BINARY(16) 性能最高
    private UUID id;

    @Column(nullable = false)
    private String tierName; // 例如："VIP", "早鸟票", "现场票"

    @Column(nullable = false)
    private BigDecimal price; // 价格建议使用 BigDecimal

    @Column(nullable = false)
    private Integer totalCapacity; // 总库存

    @Column(name = "sale_start_time")
    private OffsetDateTime saleStartTime;

    @Column(name = "sale_end_time")
    private OffsetDateTime saleEndTime;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }
}