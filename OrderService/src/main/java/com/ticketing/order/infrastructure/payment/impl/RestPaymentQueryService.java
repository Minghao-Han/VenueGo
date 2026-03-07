package com.ticketing.order.infrastructure.payment.impl;

import com.ticketing.order.common.config.AppProperties;
import com.ticketing.order.infrastructure.payment.PaymentQueryService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP-based implementation of PaymentQueryService
 * Queries the payment platform via REST API
 */
@Component("restPaymentQueryService")
@Slf4j
public class RestPaymentQueryService implements PaymentQueryService {


    private final RestTemplate restTemplate;
    private final AppProperties appProperties;

    public RestPaymentQueryService(
            RestTemplate restTemplate,
            AppProperties appProperties) {
        this.appProperties = appProperties;
        this.restTemplate = restTemplate;
    }

    @Override
    public PaymentStatus queryPaymentStatus(String paymentId) throws PaymentQueryException {
        try {
            String url = buildQueryUrl(paymentId);
            log.debug("Querying payment status from URL: {}", url);

            // Call payment platform API
            PaymentQueryResponse response = this.restTemplate.getForObject(
                    url,
                    PaymentQueryResponse.class);

            if (response == null || response.getStatus() == null) {
                throw new PaymentQueryException("Invalid response from payment platform");
            }

            PaymentStatus status = PaymentStatus.valueOf(response.getStatus().toUpperCase());
            log.debug("Payment status query succeeded: {} = {}", paymentId, status);

            return status;
        } catch (RestClientException e) {
            log.error("Failed to query payment status for payment ID: {}", paymentId, e);
            throw new PaymentQueryException("Payment query failed", e);
        }
    }

    /**
     * Build full query URL with payment ID as parameter
     */
    private String buildQueryUrl(String paymentId) {
        String baseUrl = this.appProperties.getPayment().getQueryUrl();
        return baseUrl + "?paymentId=" + paymentId;
    }

    /**
     * Response DTO from payment platform
     */
    @Data
    public static class PaymentQueryResponse {
        private String status; // PAID, UNPAID
    }
}
