package com.happy.TicketingService.mq;

import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class GeneralMessageProducer {

    private static final Logger log = LoggerFactory.getLogger(GeneralMessageProducer.class);

    private final RocketMQTemplate rocketMQTemplate;

    public GeneralMessageProducer(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    public void send(String topic, Object payload) {
        rocketMQTemplate.syncSend(topic, MessageBuilder.withPayload(payload).build());
    }

    public void sendAsync(String topic, Object payload) {
        rocketMQTemplate.asyncSend(topic, MessageBuilder.withPayload(payload).build(), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                // Keep success log at debug level to avoid log pressure in high-QPS purchase path.
                if (log.isDebugEnabled()) {
                    log.debug("mq async send success. topic={} msgId={}",
                            topic,
                            sendResult == null ? null : sendResult.getMsgId());
                }
            }

            @Override
            public void onException(Throwable e) {
                log.error("mq async send failed. topic={}", topic, e);
            }
        });
    }
}
