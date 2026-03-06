package com.happy.VenueService.event;

import java.util.List;
import java.util.UUID;

public record TicketTiersDeletedEvent(List<UUID> tierIds) {
}
