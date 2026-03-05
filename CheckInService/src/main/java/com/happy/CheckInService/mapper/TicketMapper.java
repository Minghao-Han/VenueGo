package com.happy.CheckInService.mapper;

import com.happy.CheckInService.entity.Ticket;
import org.apache.ibatis.annotations.*;

@Mapper
public interface TicketMapper {

    @Insert("""
        INSERT INTO ticket (
            ticket_code, order_id, ticket_type_id,
            user_id, event_id, status, create_time
        ) VALUES (
            #{ticketCode}, #{orderId}, #{ticketTypeId},
            #{userId}, #{eventId}, 'UNUSED', NOW()
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Ticket ticket);

    @Select("SELECT * FROM ticket WHERE ticket_code = #{ticketCode}")
    Ticket findByCode(String ticketCode);

    // 验票成功后更新，加 AND status='UNUSED' 兜底防重复
    @Update("""
        UPDATE ticket
        SET status   = 'USED',
            use_time = #{useTime}
        WHERE ticket_code = #{ticketCode}
          AND status = 'UNUSED'
        """)
    int markUsed(@Param("ticketCode") String ticketCode,
                 @Param("useTime") java.time.LocalDateTime useTime);

    @Update("""
        UPDATE ticket SET status = 'EXPIRED'
        WHERE event_id = #{eventId}
          AND status   = 'UNUSED'
        """)
    int expireByEvent(Long eventId);
}