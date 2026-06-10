package ch.ssdd.eventhub.ports.outbound;

import ch.ssdd.eventhub.domain.Event;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventPersistencePort {
    Event save(Event event);
    Optional<Event> findById(UUID uuid);
    List<Event> findAll();
    List<Event> searchByTitleOrDescription(String searchTerm);
    void deleteById(UUID uuid);
    Event updateById(UUID uuid, Event event);
}
