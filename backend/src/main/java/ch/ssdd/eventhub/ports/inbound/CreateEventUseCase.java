package ch.ssdd.eventhub.ports.inbound;

import ch.ssdd.eventhub.domain.Event;

import java.time.LocalDateTime;

public interface CreateEventUseCase {

    Event create(
            String title,
            String description,
            LocalDateTime from,
            LocalDateTime to,
            String location,
            String username);
}
