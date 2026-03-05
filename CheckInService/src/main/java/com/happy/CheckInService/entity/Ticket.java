package com.happy.CheckInService.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Ticket {

    private Long id;
    private String ticketCode;       // 二维码内容，唯一
    private Long orderId;            // 来自哪个订单
    private Long ticketTypeId;       // 票档 ID（早鸟/VIP等）
    private Long userId;
    private Long eventId;
    private String status;           // UNUSED / USED / EXPIRED / REFUNDED
    private LocalDateTime createTime;
    private LocalDateTime useTime;   // 验票时间，未使用时为 null
}