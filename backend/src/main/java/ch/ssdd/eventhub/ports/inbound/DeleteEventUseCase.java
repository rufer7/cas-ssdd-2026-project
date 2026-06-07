package ch.ssdd.eventhub.ports.inbound;

import java.util.UUID;

public interface DeleteEventUseCase {
    void deleteEvent(UUID eventId);
}
