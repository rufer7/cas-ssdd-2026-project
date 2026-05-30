package ch.ssdd.eventhub.adapters.inbound.rest.dto;

import ch.ssdd.eventhub.domain.Event;

import java.time.LocalDateTime;

public record EventDTO (
        String title,
        String description,
        String location,
        LocalDateTime date
){

    public static EventDTO of(Event event) {
        return new EventDTO(
                event.title(),
                event.description(),
                event.location(),
                event.from()
        );
    }

}
