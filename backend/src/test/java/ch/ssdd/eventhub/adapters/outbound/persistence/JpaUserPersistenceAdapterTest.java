package ch.ssdd.eventhub.adapters.outbound.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.ssdd.eventhub.domain.User;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JpaUserPersistenceAdapterTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    JpaUserPersistenceAdapter adapter;

    @Test
    void shouldMapEntityToDomainWhenUserFound() {
        User user = User.createNewProvisionedAdminUser("alice_admin");
        when(userRepository.findByUsername("alice_admin"))
                .thenReturn(Optional.of(new UserEntity(user)));

        Optional<User> result = adapter.findByUsername("alice_admin");

        assertThat(result).isPresent();
        assertThat(result.get().username()).isEqualTo("alice_admin");
    }

    @Test
    void shouldReturnEmptyWhenUserNotFound() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThat(adapter.findByUsername("missing")).isEmpty();
    }

    @Test
    void shouldReturnEmptyListForFindAll() {
        assertThat(adapter.findAll()).isEmpty();
    }

    @Test
    void shouldPersistUserOnSave() {
        User user = User.createNewProvisionedAdminUser("alice_admin");
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        when(userRepository.save(any(UserEntity.class))).thenReturn(new UserEntity(user));

        User saved = adapter.save(user);

        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().toUser().username()).isEqualTo("alice_admin");
        assertThat(saved.username()).isEqualTo("alice_admin");
    }
}
