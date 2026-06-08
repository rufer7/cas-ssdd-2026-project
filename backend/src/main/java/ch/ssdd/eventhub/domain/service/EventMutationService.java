package ch.ssdd.eventhub.domain.service;

import ch.ssdd.eventhub.domain.Event;
import ch.ssdd.eventhub.ports.inbound.DeleteEventUseCase;
import ch.ssdd.eventhub.ports.inbound.UpdateEventUseCase;
import ch.ssdd.eventhub.ports.outbound.EventPersistencePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EventMutationService implements UpdateEventUseCase, DeleteEventUseCase {

    private static final Logger logger = LoggerFactory.getLogger(EventMutationService.class);

    private final EventPersistencePort eventPersistencePort;

    public EventMutationService(EventPersistencePort eventPersistencePort) {
        this.eventPersistencePort = eventPersistencePort;
    }

    @Override
    public void deleteEvent(UUID eventId) {
        logger.debug("Executing business logic to delete event ID: '{}'", eventId);

        eventPersistencePort.deleteById(eventId);

        logger.info("Business Operation Success: Event ID '{}' deleted from the system", eventId);
    }

    @Override
    public Event update(UUID eventId, String title, String description, LocalDateTime from, LocalDateTime to, String location) {
        logger.debug("Executing business logic to update event ID: '{}'", eventId);

        Event existingEvent = eventPersistencePort.findById(eventId)
                .orElseThrow(() -> {
                    logger.warn("Event update failed: Event ID '{}' does not exist", eventId);
                    return new IllegalArgumentException("Event not found for id: " + eventId);
                });

        Event updatedEvent = existingEvent.updateDetails(title, description, from, to, location);
        Event savedEvent = eventPersistencePort.save(updatedEvent);

        logger.info("Business Operation Success: Event ID '{}' updated details saved successfully", eventId);
        return savedEvent;
    }
}
