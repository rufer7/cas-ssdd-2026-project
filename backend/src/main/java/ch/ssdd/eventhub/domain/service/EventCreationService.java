package ch.ssdd.eventhub.domain.service;

import ch.ssdd.eventhub.domain.Event;
import ch.ssdd.eventhub.domain.User;
import ch.ssdd.eventhub.ports.inbound.CreateEventUseCase;
import ch.ssdd.eventhub.ports.outbound.UserPersistencePort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EventCreationService implements CreateEventUseCase {

    private final UserPersistencePort userPersistencePort;

    public EventCreationService(UserPersistencePort userPersistencePort) {
        this.userPersistencePort = userPersistencePort;
    }

    @Override
    public Event create(String title, String description, LocalDateTime from,
                        LocalDateTime to, String location, String username) {
        User user = userPersistencePort.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found for username: " + username));
        LocalDateTime now = LocalDateTime.now();

        return new Event(
                title,
                description,
                from,
                to,
                location,
                user,
                now,
                user,
                now,
                null
        );
    }
}