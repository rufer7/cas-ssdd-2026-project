package ch.ssdd.eventhub.domain.service;

import ch.ssdd.eventhub.domain.Event;
import ch.ssdd.eventhub.domain.Role;
import ch.ssdd.eventhub.domain.User;
import ch.ssdd.eventhub.ports.outbound.EventPersistencePort;
import ch.ssdd.eventhub.ports.outbound.UserPersistencePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventCreationServiceTest {

    @Mock
    EventPersistencePort eventPersistencePort;

    @Mock
    UserPersistencePort userPersistencePort;

    @InjectMocks
    EventCreationService service;

    @Test
    void shouldCreateEventSuccessfully() {
        // given
        User user = new User(
                "john",
                "ext-1",
                Role.USER,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1)
        );

        when(userPersistencePort.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(eventPersistencePort.save(any(Event.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime from = LocalDateTime.now().plusDays(1);
        LocalDateTime to = LocalDateTime.now().plusDays(2);

        // when
        Event result = service.create(
                "My Event",
                "Description",
                from,
                to,
                "Zurich",
                "john"
        );

        // then
        assertNotNull(result);
        assertEquals("My Event", result.title());
        assertEquals("Description", result.description());
        assertEquals("Zurich", result.location());

        assertEquals(user, result.createdBy());
        assertEquals(user, result.modifiedBy());

        verify(userPersistencePort, times(1))
                .findByUsername("john");

        verify(eventPersistencePort, times(1))
                .save(any(Event.class));
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        // given
        when(userPersistencePort.findByUsername("missing"))
                .thenReturn(Optional.empty());

        // when + then
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(
                        "Event",
                        "Desc",
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2),
                        "Zurich",
                        "missing"
                )
        );

        assertTrue(ex.getMessage().contains("User not found"));

        verify(eventPersistencePort, never()).save(any());
    }

    @Test
    void shouldLoadAllEvents() {
        // given
        Event event1 = mock(Event.class);
        Event event2 = mock(Event.class);

        when(eventPersistencePort.findAll())
                .thenReturn(List.of(event1, event2));

        // when
        List<Event> result = service.loadAllEvents();

        // then
        assertEquals(2, result.size());
        assertSame(event1, result.get(0));
        assertSame(event2, result.get(1));

        verify(eventPersistencePort, times(1)).findAll();
    }
}