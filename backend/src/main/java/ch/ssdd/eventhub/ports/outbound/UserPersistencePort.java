package ch.ssdd.eventhub.ports.outbound;

import ch.ssdd.eventhub.domain.Event;
import ch.ssdd.eventhub.domain.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserPersistencePort {
    Optional<User> findByUsername(String username);
    List<User> findAll();
}
