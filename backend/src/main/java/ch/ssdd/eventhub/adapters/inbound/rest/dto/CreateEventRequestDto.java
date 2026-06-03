package ch.ssdd.eventhub.adapters.inbound.rest.dto;

import ch.ssdd.eventhub.domain.command.CreateEventCommand;

import java.time.LocalDateTime;

public record CreateEventRequestDto(
        String title,
        String description,
        LocalDateTime from,
        LocalDateTime to,
        String location,
        // TODO: to be removed as soon as authentication is in place
        String username) {

    public CreateEventCommand toCommand() {
        return new CreateEventCommand(title, description, from, to, location, username);
    }
}
