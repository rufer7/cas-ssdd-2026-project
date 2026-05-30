package ch.ssdd.eventhub.domain.service;

import ch.ssdd.eventhub.domain.Event;
import ch.ssdd.eventhub.domain.User;
import ch.ssdd.eventhub.ports.inbound.CreateEventUseCase;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EventCreationService implements CreateEventUseCase {

    @Override
    public Event create(String title, String description, LocalDateTime from,
                        LocalDateTime to, String location, User createdBy) {

        LocalDateTime now = LocalDateTime.now();

        return new Event(
                title,
                description,
                from,
                to,
                location,
                createdBy,
                now,
                createdBy,
                now,
                null
        );
    }
}