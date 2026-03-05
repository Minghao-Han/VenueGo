package com.happy.CheckInService.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.happy.CheckInService.util.UUID.IdGeneratorProvider;
import com.happy.CheckInService.util.UUID.IdGeneratorStrategy;
import com.happy.CheckInService.util.UUID.Impl.RandomIdGenerator;
import com.happy.CheckInService.util.UUID.Impl.TimeOrderedIdGenerator;

@Configuration
public class IdGeneratorConfig {

    @Bean
    public IdGeneratorStrategy idGenerator(
            @Value("${app.id-strategy:time-ordered}") String strategy,
            TimeOrderedIdGenerator timeOrdered,
            RandomIdGenerator random) {
        
        IdGeneratorStrategy selected = switch (strategy) {
            case "random" -> random;
            default -> timeOrdered;
        };
        IdGeneratorProvider.setStrategy(selected);
        return selected;
    }
}