package com.happy.CheckInService.exception;

public class TicketAlreadyUsedException extends BusinessException {
    public TicketAlreadyUsedException() {
        super(409, "该票已使用");
    }
}