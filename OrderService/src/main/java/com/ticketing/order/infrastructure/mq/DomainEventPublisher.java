package com.ticketing.order.infrastructure.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.order.domain.order.event.OrderDomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Publisher for domain events
 * 
 * Publishes domain events to RocketMQ after optimistic lock write succeeds.
 * This ensures events are only published if the order state mutation persisted successfully.
 */
@Component
public class DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(DomainEventPublisher.class);

    private final ObjectMapper objectMapper;

    public DomainEventPublisher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Publish a list of domain events
     * 
     * @param events the domain events to publish
     */
    public void publishEvents(List<OrderDomainEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        for (OrderDomainEvent event : events) {
            publishEvent(event);
        }
    }

    /**
     * Publish a single domain event
     * 
     * @param event the domain event to publish
     */
    public void publishEvent(OrderDomainEvent event) {
        try {
            String eventJson = this.objectMapper.writeValueAsString(event);
            log.debug("Publishing domain event: {}", eventJson);

            // In production, this would send to a domain event topic in RocketMQ
            // For now, just log it
            // Example RocketMQ publish:
            // rocketMQTemplate.syncSend("order_event_topic:order_event_routing_key", eventJson);

        } catch (Exception e) {
            log.error("Failed to publish domain event: {}", event, e);
            throw new RuntimeException("Failed to publish domain event", e);
        }
    }
}
