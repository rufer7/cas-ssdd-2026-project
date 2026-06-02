package ch.ssdd.eventhub.domain.service;

import ch.ssdd.eventhub.domain.Event;
import ch.ssdd.eventhub.ports.inbound.DeleteEventUseCase;
import ch.ssdd.eventhub.ports.inbound.UpdateEventUseCase;
import ch.ssdd.eventhub.ports.outbound.EventPersistencePort;
import ch.ssdd.eventhub.ports.outbound.UserPersistencePort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EventMutationService implements UpdateEventUseCase, DeleteEventUseCase {

    private final EventPersistencePort eventPersistencePort;

    public EventMutationService(EventPersistencePort eventPersistencePort) {
        this.eventPersistencePort = eventPersistencePort;
    }


    @Override
    public void deleteEvent(UUID eventId) {
            eventPersistencePort.deleteById(eventId);
    }

    @Override
    public Event update(UUID eventId, String title, String description, LocalDateTime from, LocalDateTime to, String location) {
        // 1. Fetch current domain state
        Event existingEvent = eventPersistencePort.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found for id: " + eventId));

        // 2. Execute mutation directly in the domain layer
        Event updatedEvent = existingEvent.updateDetails(title, description, from, to, location);

        // 3. Persist the already-mutated domain object
        return eventPersistencePort.save(updatedEvent);
    }
}
