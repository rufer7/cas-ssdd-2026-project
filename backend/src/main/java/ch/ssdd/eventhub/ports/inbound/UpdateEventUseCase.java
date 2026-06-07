package ch.ssdd.eventhub.ports.inbound;

import ch.ssdd.eventhub.domain.Event;

import java.time.LocalDateTime;
import java.util.UUID;

public interface UpdateEventUseCase {
    Event update(UUID id,
                 String title,
                 String description,
                 LocalDateTime from,
                 LocalDateTime to,
                 String location);
}
