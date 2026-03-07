package com.ticketing.order.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * Unified response wrapper for all REST API endpoints
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Response<T> {

    private static final String SUCCESS_CODE = "0000";
    private static final String SUCCESS_MSG = "Success";

    private String code;
    private String message;
    private T data;

    public Response() {
    }

    public Response(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ResponseBuilder<T> builder() {
        return new ResponseBuilder<>();
    }

    /**
     * Success response with data
     */
    public static <T> Response<T> success(T data) {
        return Response.<T>builder()
                .code(SUCCESS_CODE)
                .message(SUCCESS_MSG)
                .data(data)
                .build();
    }

    /**
     * Success response without data
     */
    public static <T> Response<T> success() {
        return Response.<T>builder()
                .code(SUCCESS_CODE)
                .message(SUCCESS_MSG)
                .build();
    }

    /**
     * Error response
     */
    public static <T> Response<T> error(String code, String message) {
        return Response.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    /**
     * Check if response is successful
     */
    public boolean isSuccess() {
        return SUCCESS_CODE.equals(this.code);
    }

    public static class ResponseBuilder<T> {
        private String code;
        private String message;
        private T data;

        public ResponseBuilder<T> code(String code) {
            this.code = code;
            return this;
        }

        public ResponseBuilder<T> message(String message) {
            this.message = message;
            return this;
        }

        public ResponseBuilder<T> data(T data) {
            this.data = data;
            return this;
        }

        public Response<T> build() {
            return new Response<>(code, message, data);
        }
    }
}
