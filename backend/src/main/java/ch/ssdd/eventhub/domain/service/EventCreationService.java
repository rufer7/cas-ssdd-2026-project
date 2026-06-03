package ch.ssdd.eventhub.domain.service;

import ch.ssdd.eventhub.domain.Event;
import ch.ssdd.eventhub.domain.User;
import ch.ssdd.eventhub.domain.command.CreateEventCommand;
import ch.ssdd.eventhub.ports.inbound.CreateEventUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadAllEventsUseCase;
import ch.ssdd.eventhub.ports.outbound.EventPersistencePort;
import ch.ssdd.eventhub.ports.outbound.UserPersistencePort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventCreationService implements LoadAllEventsUseCase, CreateEventUseCase {

    private final EventPersistencePort eventPersistencePort;
    private final UserPersistencePort userPersistencePort;

    public EventCreationService(EventPersistencePort eventPersistencePort, UserPersistencePort userPersistencePort) {
        this.eventPersistencePort = eventPersistencePort;
        this.userPersistencePort = userPersistencePort;
    }

    @Override
    public Event create(CreateEventCommand createEventCommand) {
        String username = createEventCommand.username();
        User user = userPersistencePort.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found for username: " + username));
        Event eventFromCommand = Event.createFromCommand(createEventCommand, user);
        return eventPersistencePort.save(eventFromCommand);
    }

    @Override
    public List<Event> loadAllEvents() {
        return eventPersistencePort.findAll();
    }
}