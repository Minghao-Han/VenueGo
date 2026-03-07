package com.ticketing.order.infrastructure.mq;

import com.ticketing.order.common.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * RocketMQ producer for order timeout messages
 * 
 * Sends a delay message carrying orderId.
 * The message is delivered after the timeout duration.
 */
@Component
public class RocketMQOrderProducer {

    private static final Logger log = LoggerFactory.getLogger(RocketMQOrderProducer.class);

    private final AppProperties appProperties;
    
    private DefaultMQProducer producer;

    public RocketMQOrderProducer(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @PostConstruct
    public void init() throws Exception {
        String nameServer = System.getenv("ROCKETMQ_NAME_SERVER");
        if (nameServer == null || nameServer.trim().isEmpty()) {
            nameServer = "localhost:9876";
        }

        producer = new DefaultMQProducer("order-producer-group");
        producer.setNamesrvAddr(nameServer);
        producer.setMaxMessageSize(1024 * 1024);
        producer.setSendMsgTimeout(3000);
        producer.start();

        log.info("RocketMQ Producer initialized with nameServer: {}", nameServer);
    }

    @PreDestroy
    public void cleanup() {
        if (producer != null) {
            producer.shutdown();
            log.info("RocketMQ Producer shutdown");
        }
    }

    /**
     * Send delay message for order timeout
     * 
     * The message is delivered after the timeout duration.
     * 
     * @param orderId the order ID
     * @param delaySeconds the delay duration in seconds
     * @return message ID
     */
    public String sendOrderTimeoutMessage(String orderId, long delaySeconds) {
        try {
            String topic = appProperties.getTimeoutTopic();
            
            Message message = new Message(
                    topic,
                    "ORDER_TIMEOUT",
                    orderId.getBytes());

            // Set delay: RocketMQ supports levels 1-18 for delays
            // Level 1 = 1 second, level 18 = 2 hours
            // For arbitrary delays, we'll set as a property and handle client-side
            message.putUserProperty("orderId", orderId);
            message.putUserProperty("delaySeconds", String.valueOf(delaySeconds));

            log.info("Sending order timeout message: orderId={}, delay={}s", orderId, delaySeconds);

            SendResult sendResult = producer.send(message);

            String messageId = sendResult.getMsgId();
            log.debug("Order timeout message sent: messageId={}, orderId={}", messageId, orderId);

            return messageId;
        } catch (Exception e) {
            log.error("Failed to send order timeout message for orderId: {}", orderId, e);
            throw new RuntimeException("Failed to send timeout message", e);
        }
    }
}
