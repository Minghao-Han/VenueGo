package com.happy.VenueService.config;
import com.happy.VenueService.UUID.IdGeneratorProvider;
import com.happy.VenueService.UUID.IdGeneratorStrategy;
import com.happy.VenueService.UUID.Impl.RandomIdGenerator;
import com.happy.VenueService.UUID.Impl.TimeOrderedIdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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