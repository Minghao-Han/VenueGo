package com.happy.UserService.service;

import java.util.List;
import java.util.UUID;

public interface DailySignService {
    void sign(UUID userId);
    int getStreak(UUID userId);
    List<Integer> getMonthRecord(UUID userId);
}
