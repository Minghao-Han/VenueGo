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
在 VenueGo 这种应用中，用户经常会查询“我附近 5 公里的演出”。

我在 latitude 和 longitude 上建立了复合索引。

建立了 city_code 索引。因为跨城市的距离计算量大且无意义，通常先通过 city_code 缩小范围。
*/
@Data
@NoArgsConstructor
@Entity
@Table(name = "venues", indexes = {
    @Index(name = "idx_venue_host", columnList = "host_id"),
    @Index(name = "idx_venue_city", columnList = "city_code"),
    @Index(name = "idx_venue_coords", columnList = "latitude, longitude"),
    @Index(name = "idx_start_time", columnList = "start_time") // 增加时间索引优化排序
})
public class Venue {

    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(columnDefinition = "BINARY(16)") // UUID 在 MySQL 中存为 BINARY(16) 性能最高
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

    // --- 活动特有属性合并至此 ---
    
    @Column(name = "start_time")
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    @Column(name = "poster_url")
    private String posterUrl; // 海报地址

    @Enumerated(EnumType.STRING)
    private VenueStatus status = VenueStatus.UPCOMING;

    /**
     * 票档嵌套：使用 @ElementCollection 或 @OneToMany
     * 这里推荐 @OneToMany 配合 orphanRemoval，方便在后台管理端直接增删票档
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "venue_id") // 在 ticket_tiers 表中生成外键
    private List<TicketTier> ticketTiers = new ArrayList<>();

    // --- 审计字段 ---

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        // 使用 uuid-creator 生成时间有序的 UUID v7
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
}
