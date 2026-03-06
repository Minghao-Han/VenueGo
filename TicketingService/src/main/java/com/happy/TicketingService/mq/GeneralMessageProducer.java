package com.happy.TicketingService.mq;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class GeneralMessageProducer {

    private final RocketMQTemplate rocketMQTemplate;

    public GeneralMessageProducer(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    public void send(String topic, Object payload) {
        rocketMQTemplate.syncSend(topic, MessageBuilder.withPayload(payload).build());
    }
}
