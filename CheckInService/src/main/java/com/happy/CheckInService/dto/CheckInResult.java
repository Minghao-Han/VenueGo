package com.happy.CheckInService.dto;

import com.happy.CheckInService.entity.Ticket;
import lombok.Data;

@Data
public class CheckInResult {

    private boolean success;
    private String  message;
    private String  ticketCode;
    private Long    userId;
    private Long    eventId;

    public static CheckInResult success(String message, Ticket ticket) {
        CheckInResult r = new CheckInResult();
        r.success    = true;
        r.message    = message;
        r.ticketCode = ticket.getTicketCode();
        r.userId     = ticket.getUserId();
        r.eventId    = ticket.getEventId();
        return r;
    }

    public static CheckInResult fail(String message) {
        CheckInResult r = new CheckInResult();
        r.success = false;
        r.message = message;
        return r;
    }
}
