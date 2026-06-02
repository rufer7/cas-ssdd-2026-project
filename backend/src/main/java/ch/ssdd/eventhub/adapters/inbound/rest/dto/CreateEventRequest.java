package ch.ssdd.eventhub.adapters.inbound.rest.dto;

import java.time.LocalDateTime;

public record CreateEventRequest(
        String title,
        String description,
        LocalDateTime from,
        LocalDateTime to,
        String location,
        // TODO: to be removed as soon as authentication is in place
        String username) {
}
