package com.happy.VenueService.UUID;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;


public class IdGeneratorProvider {
    
    private static IdGeneratorStrategy strategy;

    
    public static void setStrategy(IdGeneratorStrategy strategy) {
        IdGeneratorProvider.strategy = strategy;
    }

    public static UUID generateId() {
        if (strategy == null) {
            throw new IllegalStateException("IdGeneratorStrategy 仍未初始化！请检查 IdGeneratorConfig 是否成功创建了 Bean。");
        }
        return strategy.generateId();
    }
}