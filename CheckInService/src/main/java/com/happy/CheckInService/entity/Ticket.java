package com.happy.CheckInService.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.happy.CheckInService.util.UUID.IdGeneratorProvider;

@Data
@NoArgsConstructor
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;
    private String ticketCode;       // 二维码内容，唯一
    private Long orderId;            // 来自哪个订单
    private Long ticketTypeId;       // 票档 ID（早鸟/VIP等）
    private Long userId;
    private Long eventId;
    private String status;           // UNUSED / USED / EXPIRED / REFUNDED
    private LocalDateTime createTime;
    private LocalDateTime useTime;   // 验票时间，未使用时为 null

    @PrePersist
    protected void onCreate() {
        // 使用 uuid-creator 生成时间有序的 UUID v7
        if (this.id == null) {
            this.id = IdGeneratorProvider.generateId();
        }
        if (this.createTime == null) {
            this.createTime = LocalDateTime.now();
        }
    }
}