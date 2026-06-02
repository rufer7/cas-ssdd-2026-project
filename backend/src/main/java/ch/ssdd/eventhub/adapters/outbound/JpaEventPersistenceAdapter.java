package ch.ssdd.eventhub.adapters.outbound;

import ch.ssdd.eventhub.adapters.outbound.jpa.EventEntity;
import ch.ssdd.eventhub.adapters.outbound.jpa.EventRepository;
import ch.ssdd.eventhub.adapters.outbound.jpa.UserEntity;
import ch.ssdd.eventhub.adapters.outbound.jpa.UserRepository;
import ch.ssdd.eventhub.domain.Event;
import ch.ssdd.eventhub.ports.outbound.EventPersistencePort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JpaEventPersistenceAdapter implements EventPersistencePort {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public JpaEventPersistenceAdapter(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Event save(Event event) {
        // TODO: to be refactored as soon as we get the user from authentication context
        String usernameCreatedBy = event.createdBy().username();

        UserEntity createdBy = userRepository.findByUsername(usernameCreatedBy)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + usernameCreatedBy));

        EventEntity entity = new EventEntity(event, createdBy, createdBy);
        eventRepository.save(entity);
        return event;
    }

    @Override
    public Optional<Event> findById(UUID uuid) {
        return eventRepository
                .findById(uuid)
                .map(EventEntity::toEvent);
    }

    @Override
    public List<Event> findAll() {
        return eventRepository.findAll()
                .stream()
                .map(EventEntity::toEvent)
                .toList();
    }
}