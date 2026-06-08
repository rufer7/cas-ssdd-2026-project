package ch.ssdd.eventhub.adapters.outbound.persistence;

import ch.ssdd.eventhub.common.LocalDateTimeHelper;
import ch.ssdd.eventhub.domain.Event;
import ch.ssdd.eventhub.domain.Role;
import ch.ssdd.eventhub.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaEventPersistenceAdapterTest {

        @Mock
        EventRepository eventRepository;

        @Mock
        UserRepository userRepository;

        @InjectMocks
        JpaEventPersistenceAdapter adapter;

        @Test
        void shouldSaveEventSuccessfully() {
                // given
                User user = new User(
                                "john",
                                "ext-1",
                                Role.USER,
                                LocalDateTimeHelper.utcNow().minusDays(1),
                                LocalDateTimeHelper.utcNow().minusDays(1));

                Event event = new Event(
                        UUID.fromString("00000000-0000-0000-0000-000000000001"),
                                "title",
                                "desc",
                                LocalDateTimeHelper.utcNow().plusDays(1),
                                LocalDateTimeHelper.utcNow().plusDays(2),
                                "Zurich",
                                user,
                                LocalDateTimeHelper.utcNow(),
                                user,
                                LocalDateTimeHelper.utcNow(),
                                null);

                var userEntity = new UserEntity(user);

                when(userRepository.findByUsername("john"))
                                .thenReturn(Optional.of(userEntity));

                when(eventRepository.save(any(EventEntity.class)))
                                .thenAnswer(inv -> inv.getArgument(0));

                // when
                Event result = adapter.save(event);

                // then
                assertNotNull(result);
                assertEquals("title", result.title());

                verify(userRepository, times(1)).findByUsername("john");

                ArgumentCaptor<EventEntity> captor = ArgumentCaptor.forClass(EventEntity.class);
                verify(eventRepository).save(captor.capture());

                EventEntity savedEntity = captor.getValue();
                assertEquals("title", savedEntity.getTitle());
                assertEquals("Zurich", savedEntity.getLocation());
        }

        @Test
        void shouldThrowWhenUserNotFound() {
                // given
                User user = new User(
                                "john",
                                "ext-1",
                                Role.USER,
                                LocalDateTimeHelper.utcNow().minusDays(1),
                                LocalDateTimeHelper.utcNow().minusDays(1));

                Event event = new Event(
                        UUID.fromString("00000000-0000-0000-0000-000000000002"),
                        "title",
                                "desc",
                                LocalDateTimeHelper.utcNow().plusDays(1),
                                LocalDateTimeHelper.utcNow().plusDays(2),
                                "Zurich",
                                user,
                                LocalDateTimeHelper.utcNow(),
                                user,
                                LocalDateTimeHelper.utcNow(),
                                null);

                when(userRepository.findByUsername("john"))
                                .thenReturn(Optional.empty());

                // when + then
                assertThrows(
                                IllegalArgumentException.class,
                                () -> adapter.save(event));

                verify(eventRepository, never()).save(any());
        }

        @Test
        void shouldFindById() {
                // given
                UUID id = UUID.randomUUID();

                EventEntity entity = mock(EventEntity.class);
                Event domainEvent = mock(Event.class);

                when(entity.toEvent()).thenReturn(domainEvent);

                when(eventRepository.findById(id))
                                .thenReturn(Optional.of(entity));

                // when
                Optional<Event> result = adapter.findById(id);

                // then
                assertTrue(result.isPresent());
                assertEquals(domainEvent, result.get());

                verify(eventRepository).findById(id);
        }

        @Test
        void shouldFindAllEvents() {
                // given
                EventEntity e1 = mock(EventEntity.class);
                EventEntity e2 = mock(EventEntity.class);

                Event d1 = mock(Event.class);
                Event d2 = mock(Event.class);

                when(e1.toEvent()).thenReturn(d1);
                when(e2.toEvent()).thenReturn(d2);

                when(eventRepository.findAll())
                                .thenReturn(List.of(e1, e2));

                // when
                List<Event> result = adapter.findAll();

                // then
                assertEquals(2, result.size());
                assertSame(d1, result.get(0));
                assertSame(d2, result.get(1));

                verify(eventRepository).findAll();
        }
}
