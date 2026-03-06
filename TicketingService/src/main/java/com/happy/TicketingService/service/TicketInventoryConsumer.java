package com.happy.TicketingService.service;

import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import com.happy.TicketingService.event.TicketPurchaseEvent;

@Component
@RocketMQMessageListener(
        topic = "${ticketing.mq.purchase-topic}",
        consumerGroup = "${ticketing.mq.inventory-consumer-group}",
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.ORDERLY)
public class TicketInventoryConsumer implements RocketMQListener<TicketPurchaseEvent> {

    private final InventoryService inventoryService;

    public TicketInventoryConsumer(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Override
    public void onMessage(TicketPurchaseEvent event) {
        inventoryService.decreaseInventoryByPurchase(event);
    }
}
