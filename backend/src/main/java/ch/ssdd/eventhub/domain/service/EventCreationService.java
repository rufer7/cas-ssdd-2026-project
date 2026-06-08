package ch.ssdd.eventhub.domain.service;

import ch.ssdd.eventhub.domain.Event;
import ch.ssdd.eventhub.domain.User;
import ch.ssdd.eventhub.domain.command.CreateEventCommand;
import ch.ssdd.eventhub.ports.inbound.CreateEventUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadAllEventsUseCase;
import ch.ssdd.eventhub.ports.outbound.EventPersistencePort;
import ch.ssdd.eventhub.ports.outbound.UserPersistencePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventCreationService implements LoadAllEventsUseCase, CreateEventUseCase {

    private static final Logger logger = LoggerFactory.getLogger(EventCreationService.class);

    private final EventPersistencePort eventPersistencePort;
    private final UserPersistencePort userPersistencePort;

    public EventCreationService(EventPersistencePort eventPersistencePort, UserPersistencePort userPersistencePort) {
        this.eventPersistencePort = eventPersistencePort;
        this.userPersistencePort = userPersistencePort;
    }

    @Override
    public Event create(CreateEventCommand createEventCommand) {
        String username = createEventCommand.username();
        logger.debug("Processing event creation business logic for user: '{}'", username);

        User user = userPersistencePort.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Event creation failed: User '{}' does not exist in the system", username);
                    return new IllegalArgumentException("User not found for username: " + username);
                });

        Event eventFromCommand = Event.createFromCommand(createEventCommand, user);
        Event savedEvent = eventPersistencePort.save(eventFromCommand);

        logger.info("Business Operation Success: Domain Event '{}' persisted with ID '{}' for user '{}'",
                savedEvent.title(), savedEvent.title(), username);

        return savedEvent;
    }

    @Override
    public List<Event> loadAllEvents() {
        List<Event> events = eventPersistencePort.findAll();
        logger.debug("Loaded {} events from persistence layer", events.size());
        return events;
    }
}
