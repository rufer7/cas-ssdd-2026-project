package ch.ssdd.eventhub.adapters.outbound;

import ch.ssdd.eventhub.adapters.outbound.jpa.EventEntity;
import ch.ssdd.eventhub.adapters.outbound.jpa.EventRepository;
import ch.ssdd.eventhub.domain.Event;
import ch.ssdd.eventhub.ports.outbound.EventPersistencePort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JpaEventPersistenceAdapter implements EventPersistencePort {

    private final EventRepository eventRepository;

    public JpaEventPersistenceAdapter(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public Event save(Event event) {
        EventEntity entity = new EventEntity(event);
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