package ch.ssdd.eventhub.domain.service;

import ch.ssdd.eventhub.domain.Event;
import ch.ssdd.eventhub.domain.User;
import ch.ssdd.eventhub.ports.inbound.CreateEventUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadAllEventsUseCase;
import ch.ssdd.eventhub.ports.outbound.EventPersistencePort;
import ch.ssdd.eventhub.ports.outbound.UserPersistencePort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    public Event create(String title, String description, LocalDateTime from,
            LocalDateTime to, String location, String username) {
        User user = userPersistencePort.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found for username: " + username));
        LocalDateTime now = LocalDateTime.now();

        Event event = new Event(title, description, from, to, location, user, now, user, now, null);

        return eventPersistencePort.save(event);
    }

    @Override
    public List<Event> loadAllEvents() {
        return eventPersistencePort.findAll();
    }
}