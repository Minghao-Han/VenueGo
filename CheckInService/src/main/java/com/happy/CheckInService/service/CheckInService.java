package com.happy.CheckInService.service;

import com.happy.CheckInService.dto.CheckInResult;
import com.happy.CheckInService.entity.Ticket;
import com.happy.CheckInService.mapper.TicketMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInService {

    private final StringRedisTemplate redisTemplate;
    private final TicketMapper ticketMapper;

    /**
     * Lua 脚本：GET + SET 原子执行，防止并发重复验票
     *
     * KEYS[1] = ticket:{code}         票状态
     * KEYS[2] = ticket:checkin:{code} 验票时间记录
     * ARGV[1] = 验票时间
     *
     * 返回值：
     *  1  验票成功
     * -1  票不存在（伪造或已过期被清除）
     * -2  已使用
     */
    private static final String CHECK_IN_SCRIPT = """
            local status = redis.call('GET', KEYS[1])
            if status == false then
                return -1
            end
            if status == '1' then
                return -2
            end
            redis.call('SET', KEYS[1], '1')
            redis.call('SET', KEYS[2], ARGV[1])
            return 1
            """;

    private final RedisScript<Long> checkInScript =
            RedisScript.of(CHECK_IN_SCRIPT, Long.class);

    /**
     * 验票入口
     */
    public CheckInResult checkIn(String ticketCode) {

        String ticketKey = "ticket:" + ticketCode;
        String recordKey = "ticket:checkin:" + ticketCode;
        String checkTime = LocalDateTime.now().toString();

        // 1. Redis 原子验票
        Long result = redisTemplate.execute(
                checkInScript,
                List.of(ticketKey, recordKey),
                checkTime
        );

        if (result == null) {
            return CheckInResult.fail("系统异常，请重试");
        }

        return switch (result.intValue()) {
            case 1  -> handleSuccess(ticketCode);
            case -1 -> CheckInResult.fail("无效票码");
            case -2 -> CheckInResult.fail("该票已使用");
            default -> CheckInResult.fail("系统异常");
        };
    }

    /**
     * 验票成功：返回结果 + 异步写 MySQL
     */
    private CheckInResult handleSuccess(String ticketCode) {
        // 从 Redis 元数据中读取票信息，直接返回给前端展示
        String metaKey = "ticket:meta:" + ticketCode;
        Map<Object, Object> meta = redisTemplate.opsForHash().entries(metaKey);

        Ticket ticket = new Ticket();
        ticket.setTicketCode(ticketCode);
        if (!meta.isEmpty()) {
            ticket.setUserId(Long.parseLong((String) meta.get("userId")));
            ticket.setEventId(Long.parseLong((String) meta.get("eventId")));
            ticket.setTicketTypeId(Long.parseLong((String) meta.get("ticketTypeId")));
        }

        // 异步写回 MySQL，不阻塞验票响应
        syncToDb(ticketCode);

        return CheckInResult.success("验票成功", ticket);
    }

    /**
     * 异步将验票结果持久化到 MySQL
     * Redis 是快车道，MySQL 是永久存档
     */
    @Async
    public void syncToDb(String ticketCode) {
        try {
            int rows = ticketMapper.markUsed(ticketCode, LocalDateTime.now());
            if (rows == 0) {
                // AND status='UNUSED' 未命中，说明极端并发下 MySQL 兜底生效
                log.warn("ticket already used in DB, code={}", ticketCode);
            }
        } catch (Exception e) {
            // 写库失败不影响入场，记录日志后由补偿任务处理
            log.error("syncToDb failed, ticketCode={}", ticketCode, e);
        }
    }
}