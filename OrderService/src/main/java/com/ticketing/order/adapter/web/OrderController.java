package com.ticketing.order.adapter.web;

import com.ticketing.order.app.command.CancelOrderCmd;
import com.ticketing.order.app.command.CreateOrderCmd;
import com.ticketing.order.app.command.PayOrderCmd;
import com.ticketing.order.app.command.UseTicketCmd;
import com.ticketing.order.app.dto.OrderDTO;
import com.ticketing.order.app.service.OrderCommandService;
import com.ticketing.order.common.response.Response;
import com.ticketing.order.domain.order.enums.OrderEvent;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * REST API endpoints for order service
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderCommandService orderCommandService;

    public OrderController(OrderCommandService orderCommandService) {
        this.orderCommandService = orderCommandService;
    }

    /**
     * POST /orders - Create a new order
     */
    @PostMapping
    public Response<OrderDTO> createOrder(@Valid @RequestBody CreateOrderCmd cmd) {
        try {
            OrderDTO order = this.orderCommandService.createOrder(cmd);
            return Response.success(order);
        } catch (Exception e) {
            return Response.error("CREATE_ORDER_FAILED", e.getMessage());
        }
    }

    /**
     * POST /orders/{id}/pay - Frontend polling fallback: trigger PAY event
     */
    @PostMapping("/{id}/pay")
    public Response<OrderDTO> payOrder(
            @PathVariable String id,
            @Valid @RequestBody PayOrderCmd cmd) {
        try {
            // Load order first to get current version
            OrderDTO currentOrder = this.orderCommandService.getOrder(id);
            OrderDTO order = this.orderCommandService.executePaymentTransition(
                    id,
                    currentOrder.getVersion(),
                    cmd.getPaymentId(),
                    cmd.getAmount());

            return Response.success(order);
        } catch (IllegalStateException e) {
            return Response.error("INVALID_STATE", e.getMessage());
        } catch (Exception e) {
            return Response.error("PAY_FAILED", e.getMessage());
        }
    }

    /**
     * POST /orders/{id}/cancel - User-initiated cancel
     */
    @PostMapping("/{id}/cancel")
    public Response<OrderDTO> cancelOrder(
            @PathVariable String id,
            @RequestBody(required = false) CancelOrderCmd cmd) {
        try {
            String reason = (cmd != null && cmd.getReason() != null)
                    ? cmd.getReason()
                    : "User initiated cancellation";

            // Load order first to get current version
            OrderDTO currentOrder = this.orderCommandService.getOrder(id);
            OrderDTO order = this.orderCommandService.executeCancelTransition(
                    id,
                    currentOrder.getVersion(),
                    reason);

            return Response.success(order);
        } catch (IllegalStateException e) {
            return Response.error("INVALID_STATE", e.getMessage());
        } catch (Exception e) {
            return Response.error("CANCEL_FAILED", e.getMessage());
        }
    }

    /**
     * POST /orders/{id}/use - Verify and use ticket
     */
    @PostMapping("/{id}/use")
    public Response<OrderDTO> useTicket(
            @PathVariable String id,
            @Valid @RequestBody UseTicketCmd cmd) {
        try {
            // Load order first to get current version
            OrderDTO currentOrder = this.orderCommandService.getOrder(id);
            OrderDTO order = this.orderCommandService.executeUseTransition(
                    id,
                    currentOrder.getVersion(),
                    cmd.getVerifyCode());

            return Response.success(order);
        } catch (IllegalStateException e) {
            return Response.error("INVALID_STATE", e.getMessage());
        } catch (IllegalArgumentException e) {
            return Response.error("INVALID_VERIFY_CODE", e.getMessage());
        } catch (Exception e) {
            return Response.error("USE_FAILED", e.getMessage());
        }
    }

    /**
     * GET /orders/{id} - Query order detail
     */
    @GetMapping("/{id}")
    public Response<OrderDTO> getOrder(@PathVariable String id) {
        try {
            OrderDTO order = this.orderCommandService.getOrder(id);
            return Response.success(order);
        } catch (IllegalArgumentException e) {
            return Response.error("ORDER_NOT_FOUND", "Order " + id + " not found");
        } catch (Exception e) {
            return Response.error("GET_FAILED", e.getMessage());
        }
    }
}

