package com.happy.CheckInService.exception;

import com.happy.CheckInService.dto.CheckInResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<CheckInResult> handleNotFound(TicketNotFoundException e) {
        return ResponseEntity.status(404).body(CheckInResult.fail(e.getMessage()));
    }

    @ExceptionHandler(TicketAlreadyUsedException.class)
    public ResponseEntity<CheckInResult> handleAlreadyUsed(TicketAlreadyUsedException e) {
        return ResponseEntity.status(409).body(CheckInResult.fail(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CheckInResult> handleGeneral(Exception e) {
        return ResponseEntity.status(500).body(CheckInResult.fail("系统异常，请重试"));
    }
}