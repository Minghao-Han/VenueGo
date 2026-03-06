package com.happy.TicketingService.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ticketing.mq")
public class TicketingMqTopicConfig {

    private String purchaseTopic;
}
