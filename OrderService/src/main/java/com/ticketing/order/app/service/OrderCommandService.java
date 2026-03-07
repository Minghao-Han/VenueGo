package com.ticketing.order.app.service;

import com.ticketing.order.app.command.*;
import com.ticketing.order.app.dto.OrderDTO;
import com.ticketing.order.common.config.AppProperties;
import com.ticketing.order.common.util.TimeProvider;
import com.ticketing.order.domain.order.aggregate.OrderAggregate;
import com.ticketing.order.domain.order.enums.OrderEvent;
import com.ticketing.order.domain.order.enums.OrderStatus;
import com.ticketing.order.domain.statemachine.OrderStateMachineBuilder;
import com.ticketing.order.domain.statemachine.action.OrderAction;
import com.ticketing.order.infrastructure.mq.DomainEventPublisher;
import com.ticketing.order.infrastructure.mq.RocketMQOrderProducer;
import com.ticketing.order.infrastructure.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Order command service - application layer
 */
@Service
public class OrderCommandService {

    private final OrderRepository orderRepository;
    private final OrderStateMachineBuilder stateMachineBuilder;
    private final DomainEventPublisher domainEventPublisher;
    private final RocketMQOrderProducer rocketMQOrderProducer;
    private final AppProperties appProperties;
    private final TimeProvider timeProvider;

    public OrderCommandService(
            OrderRepository orderRepository,
            OrderStateMachineBuilder stateMachineBuilder,
            DomainEventPublisher domainEventPublisher,
            RocketMQOrderProducer rocketMQOrderProducer,
            AppProperties appProperties,
            TimeProvider timeProvider) {
        this.orderRepository = orderRepository;
        this.stateMachineBuilder = stateMachineBuilder;
        this.domainEventPublisher = domainEventPublisher;
        this.rocketMQOrderProducer = rocketMQOrderProducer;
        this.appProperties = appProperties;
        this.timeProvider = timeProvider;
    }

    /**
     * Create a new order
     */
    public OrderDTO createOrder(CreateOrderCmd cmd) {
        String orderId = UUID.randomUUID().toString();
        String verifyCode = generateVerifyCode();

        BigDecimal totalAmount = cmd.getUnitPrice().multiply(
                new BigDecimal(cmd.getQuantity()));

        OrderAggregate order = new OrderAggregate();
        order.setOrderId(orderId);
        order.setEventId(cmd.getEventId());
        order.setVenueId(cmd.getVenueId());
        order.setTicketTypeId(cmd.getTicketTypeId());
        order.setQuantity(cmd.getQuantity());
        order.setUnitPrice(cmd.getUnitPrice());
        order.setTotalAmount(totalAmount);
        order.setVerifyCode(verifyCode);
        order.setStatus(OrderStatus.PENDING_PAY);
        order.setCreatedAt(this.timeProvider.now());
        order.setVersion(0);

        this.orderRepository.save(order);

        long delaySeconds = this.appProperties.getPayTimeoutMinutes() * 60L;
        this.rocketMQOrderProducer.sendOrderTimeoutMessage(orderId, delaySeconds);

        return aggregateToDTO(order);
    }

    /**
     * Handle payment event
     */
    public OrderDTO executePaymentTransition(
            String orderId,
            Integer expectedVersion,
            String paymentId,
            BigDecimal amount) {

        return executeStateTransition(
                orderId,
                expectedVersion,
                OrderEvent.PAY,
                paymentId,
                amount);
    }

    /**
     * Handle timeout event
     */
    public OrderDTO executeTimeoutTransition(
            String orderId,
            Integer expectedVersion,
            String reason) {

        return executeStateTransition(
                orderId,
                expectedVersion,
                OrderEvent.TIMEOUT,
                reason);
    }

    /**
     * Handle cancel event
     */
    public OrderDTO executeCancelTransition(
            String orderId,
            Integer expectedVersion,
            String reason) {

        return executeStateTransition(
                orderId,
                expectedVersion,
                OrderEvent.CANCEL,
                reason);
    }

    /**
     * Handle use event
     */
    public OrderDTO executeUseTransition(
            String orderId,
            Integer expectedVersion,
            String verifyCode) {

        return executeStateTransition(
                orderId,
                expectedVersion,
                OrderEvent.USE,
                verifyCode);
    }

    /**
     * Generic state transition executor with reason
     */
    public OrderDTO executeStateTransition(
            String orderId,
            Integer expectedVersion,
            OrderEvent event,
            String reason) {

        OrderAction.ActionContext context = new OrderAction.ActionContext();
        context.setReason(reason);
        return executeStateTransitionWithContext(orderId, expectedVersion, event, context);
    }

    /**
     * Generic state transition executor for payment events
     */
    public OrderDTO executeStateTransition(
            String orderId,
            Integer expectedVersion,
            OrderEvent event,
            String paymentId,
            BigDecimal amount) {

        OrderAction.ActionContext context = new OrderAction.ActionContext();
        context.setPaymentId(paymentId);
        context.setAmount(amount);
        return executeStateTransitionWithContext(orderId, expectedVersion, event, context);
    }

    /**
     * Generic state transition executor for use events
     */
    public OrderDTO executeUseStateTransition(
            String orderId,
            Integer expectedVersion,
            OrderEvent event,
            String verifyCode) {

        OrderAction.ActionContext context = new OrderAction.ActionContext();
        context.setVerifyCode(verifyCode);
        return executeStateTransitionWithContext(orderId, expectedVersion, event, context);
    }

    /**
     * Core state transition logic with optimistic lock retry
     */
    private OrderDTO executeStateTransitionWithContext(
            String orderId,
            Integer expectedVersion,
            OrderEvent event,
            OrderAction.ActionContext context) {

        int maxRetries = this.appProperties.getOptimisticLockMaxRetries();
        OrderAggregate order = null;

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            Optional<OrderAggregate> optionalOrder = this.orderRepository.findById(orderId);
            if (optionalOrder.isEmpty()) {
                throw new IllegalArgumentException("Order not found: " + orderId);
            }

            order = optionalOrder.get();
            Integer currentVersion = order.getVersion();

            boolean transitionAllowed = this.stateMachineBuilder.fireEvent(order, event, context);

            if (!transitionAllowed) {
                throw new IllegalStateException(
                        "State transition not allowed: " + order.getStatus() + " + " + event);
            }

            boolean updateSuccess = this.orderRepository.updateWithOptimisticLock(order, currentVersion);

            if (updateSuccess) {
                order.getPendingSideEffectsAndClear().forEach(OrderAggregate.SideEffect::execute);
                this.domainEventPublisher.publishEvents(order.getPendingEventsAndClear());
                return aggregateToDTO(order);
            } else {
                order = this.orderRepository.findById(orderId).orElseThrow();
            }
        }

        throw new RuntimeException("Optimistic lock retries exhausted for order: " + orderId);
    }

    /**
     * Query order by ID
     */
    public OrderDTO getOrder(String orderId) {
        Optional<OrderAggregate> optionalOrder = this.orderRepository.findById(orderId);
        if (optionalOrder.isEmpty()) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }

        return aggregateToDTO(optionalOrder.get());
    }

    /**
     * Convert aggregate to DTO
     */
    private OrderDTO aggregateToDTO(OrderAggregate aggregate) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(aggregate.getOrderId());
        dto.setEventId(aggregate.getEventId());
        dto.setVenueId(aggregate.getVenueId());
        dto.setTicketTypeId(aggregate.getTicketTypeId());
        dto.setStatus(aggregate.getStatus().getCode());
        dto.setQuantity(aggregate.getQuantity());
        dto.setUnitPrice(aggregate.getUnitPrice());
        dto.setTotalAmount(aggregate.getTotalAmount());
        dto.setPaymentId(aggregate.getPaymentId());
        dto.setVerifyCode(aggregate.getVerifyCode());
        dto.setCreatedAt(aggregate.getCreatedAt());
        dto.setPaidAt(aggregate.getPaidAt());
        dto.setUsedAt(aggregate.getUsedAt());
        dto.setCancelledAt(aggregate.getCancelledAt());
        dto.setVersion(aggregate.getVersion());
        return dto;
    }

    /**
     * Generate a unique verify code for ticket
     */
    private String generateVerifyCode() {
        return UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
}
