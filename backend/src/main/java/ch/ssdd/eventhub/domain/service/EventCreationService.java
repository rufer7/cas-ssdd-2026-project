package ch.ssdd.eventhub.domain.service;

import ch.ssdd.eventhub.domain.Event;
import ch.ssdd.eventhub.domain.User;
import ch.ssdd.eventhub.domain.command.CreateEventCommand;
import ch.ssdd.eventhub.ports.inbound.CreateEventUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadAllEventsUseCase;
import ch.ssdd.eventhub.ports.inbound.SearchEventsUseCase;
import ch.ssdd.eventhub.ports.outbound.EventPersistencePort;
import ch.ssdd.eventhub.ports.outbound.UserPersistencePort;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EventCreationService implements LoadAllEventsUseCase, CreateEventUseCase, SearchEventsUseCase {

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

        logger.debug("Processing event creation business logic for user '{}' ...", username);

        User user = userPersistencePort.findByUsername(username)
                .orElseGet(() -> {
                    logger.info("User '{}' not found locally. Creating new admin user before creating new event.", username);
                    User newUser = User.createNewProvisionedAdminUser(username);
                    return userPersistencePort.save(newUser);
                });

        Event eventFromCommand = Event.createFromCommand(createEventCommand, user);
        Event savedEvent = eventPersistencePort.save(eventFromCommand);

        logger.info("Processing event creation business logic for user '{}' SUCCEEDED. Event '{}' persisted with ID '{}'",
                username, savedEvent.title(), savedEvent.title());

        return savedEvent;
    }

    @Override
    public List<Event> loadAllEvents() {
        logger.debug("Loading events ...");
        var events = eventPersistencePort.findAll();
        logger.info("Loading events SUCCEEDED ({} events found)", events.size());
        return events;
    }

    @Override
    public List<Event> searchEvents(String searchString) {
        logger.debug("Searching for events with search term '{}'", searchString);
        var events = eventPersistencePort.searchByTitleOrDescription(searchString);
        logger.info("Searching for events with search term '{}' SUCCEEDED ({} events found)", searchString, events.size());
        return events;
    }
}
