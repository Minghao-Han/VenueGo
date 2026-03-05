package com.happy.UserService.service.impl;

import com.happy.UserService.service.DailySignService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DailySignServiceImpl implements DailySignService {

    private final StringRedisTemplate redisTemplate;

    // key格式: daily:sign:123:202503
    private String buildKey(UUID userId) {
        String yearMonth = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMM"));
        return "daily:sign:" + userId.toString() + ":" + yearMonth;
    }

    // 今日签到
    @Override
    public void sign(UUID userId) {
        String key = buildKey(userId);
        int dayOfMonth = LocalDateTime.now().getDayOfMonth();
        redisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
    }

    // 连续签到天数（从今天往前数连续1的个数）
    @Override
    public int getStreak(UUID userId) {
        String key = buildKey(userId);
        int dayOfMonth = LocalDateTime.now().getDayOfMonth();

        List<Long> result = redisTemplate.opsForValue().bitField(
                key,
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth))
                        .valueAt(0)
        );

        if (result == null || result.isEmpty() || result.get(0) == null || result.get(0) == 0) {
            return 0;
        }

        long num = result.get(0);
        int count = 0;
        while (true) {
            if ((num & 1) == 0) break;
            count++;
            num >>>= 1;
        }
        return count;
    }

    // 本月哪几天签到了，返回天数列表如 [1, 2, 5, 6]
    @Override
    public List<Integer> getMonthRecord(UUID userId) {
        String key = buildKey(userId);
        int totalDays = LocalDateTime.now().getDayOfMonth();

        List<Integer> signedDays = new ArrayList<>();
        for (int i = 0; i < totalDays; i++) {
            Boolean signed = redisTemplate.opsForValue().getBit(key, i);
            if (Boolean.TRUE.equals(signed)) {
                signedDays.add(i + 1);
            }
        }
        return signedDays;
    }
}
