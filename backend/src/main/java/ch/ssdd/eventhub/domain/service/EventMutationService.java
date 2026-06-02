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
        return null;
    }
}
