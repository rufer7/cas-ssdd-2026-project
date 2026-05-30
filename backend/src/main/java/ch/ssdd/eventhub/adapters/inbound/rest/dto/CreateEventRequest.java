package ch.ssdd.eventhub.adapters.inbound.rest.dto;

import java.time.LocalDateTime;

public record CreateEventRequest(
        String title,
        String description,
        LocalDateTime from,
        LocalDateTime to,
        String location,
        String username,
        String externalId
) {}