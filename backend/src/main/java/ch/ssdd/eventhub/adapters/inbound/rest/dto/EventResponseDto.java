package ch.ssdd.eventhub.adapters.inbound.rest.dto;

import ch.ssdd.eventhub.domain.Event;

import java.time.LocalDateTime;

public record EventResponseDto(
        String title,
        String description,
        LocalDateTime from,
        LocalDateTime to,
        String location) {

    public static EventResponseDto of(Event event) {
        return new EventResponseDto(
                event.title(),
                event.description(),
                event.from(),
                event.to(),
                event.location());
    }
}
