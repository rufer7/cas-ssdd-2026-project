package ch.ssdd.eventhub.ports.outbound;

import ch.ssdd.eventhub.domain.User;
import java.util.List;
import java.util.Optional;

public interface UserPersistencePort {
    Optional<User> findByUsername(String username);

    List<User> findAll();

    User save(User user);
}
