package com.happy.CheckInService.exception;

public class TicketNotFoundException extends BusinessException {
    public TicketNotFoundException() {
        super(404, "无效票码");
    }
}