package ch.ssdd.eventhub.adapters.outbound;

import ch.ssdd.eventhub.adapters.outbound.jpa.EventEntity;
import ch.ssdd.eventhub.adapters.outbound.jpa.EventRepository;
import ch.ssdd.eventhub.adapters.outbound.jpa.UserEntity;
import ch.ssdd.eventhub.adapters.outbound.jpa.UserRepository;
import ch.ssdd.eventhub.domain.Event;
import ch.ssdd.eventhub.domain.User;
import ch.ssdd.eventhub.ports.outbound.EventPersistencePort;
import ch.ssdd.eventhub.ports.outbound.UserPersistencePort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JpaUserPersistenceAdapter implements UserPersistencePort {

    private final UserRepository userRepository;

    public JpaUserPersistenceAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(UserEntity::toUser);
    }

    @Override
    public List<User> findAll() {
        return List.of();
    }
}