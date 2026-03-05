package com.happy.VenueService.exception;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;

@ControllerAdvice
public class GraphQlExceptionAdvice {

    @GraphQlExceptionHandler(BusinessException.class)
    public GraphQLError handleBusinessException(BusinessException ex, DataFetchingEnvironment env) {
        return GraphqlErrorBuilder.newError(env)
                .errorType(mapErrorType(ex))
                .message(ex.getMessage())
                .extensions(Map.of("status", ex.getStatus().value()))
                .build();
    }

    @GraphQlExceptionHandler(InterruptedException.class)
    public GraphQLError handleInterruptedException(InterruptedException ex, DataFetchingEnvironment env) {
        Thread.currentThread().interrupt();
        return GraphqlErrorBuilder.newError(env)
                .errorType(ErrorType.INTERNAL_ERROR)
                .message("Thread interrupted")
                .build();
    }

    @GraphQlExceptionHandler(BindException.class)
    public GraphQLError handleBindException(BindException ex, DataFetchingEnvironment env) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toErrorMessage)
                .collect(Collectors.joining("; "));

        if (message.isBlank()) {
            message = "Invalid request arguments";
        }

        return GraphqlErrorBuilder.newError(env)
                .errorType(ErrorType.BAD_REQUEST)
                .message(message)
                .extensions(Map.of("status", 400))
                .build();
    }

    private String toErrorMessage(FieldError error) {
        String field = error.getField();
        String defaultMessage = error.getDefaultMessage();
        if (defaultMessage == null || defaultMessage.isBlank()) {
            defaultMessage = "invalid value";
        }
        return field + ": " + defaultMessage;
    }

    private ErrorType mapErrorType(BusinessException ex) {
        return switch (ex.getStatus()) {
            case BAD_REQUEST -> ErrorType.BAD_REQUEST;
            case UNAUTHORIZED -> ErrorType.UNAUTHORIZED;
            case FORBIDDEN -> ErrorType.FORBIDDEN;
            case NOT_FOUND -> ErrorType.NOT_FOUND;
            default -> ErrorType.INTERNAL_ERROR;
        };
    }
}