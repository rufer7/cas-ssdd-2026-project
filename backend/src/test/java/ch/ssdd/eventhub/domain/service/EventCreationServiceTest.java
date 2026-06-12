package ch.ssdd.eventhub.domain.service;

import ch.ssdd.eventhub.common.LocalDateTimeHelper;
import ch.ssdd.eventhub.domain.Event;
import ch.ssdd.eventhub.domain.Role;
import ch.ssdd.eventhub.domain.User;
import ch.ssdd.eventhub.domain.command.CreateEventCommand;
import ch.ssdd.eventhub.ports.outbound.EventPersistencePort;
import ch.ssdd.eventhub.ports.outbound.UserPersistencePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
                LocalDateTimeHelper.utcNow().minusDays(1),
                LocalDateTimeHelper.utcNow().minusDays(1)
        );

        when(userPersistencePort.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(eventPersistencePort.save(any(Event.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime from = LocalDateTimeHelper.utcNow().plusDays(1);
        LocalDateTime to = LocalDateTimeHelper.utcNow().plusDays(2);
        CreateEventCommand createEventCommand = new CreateEventCommand("My Event",
                "Description",
                from,
                to,
                "Zurich",
                "john");

        // when
        Event result = service.create(createEventCommand);

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
        LocalDateTime plusOne = LocalDateTimeHelper.utcNow().plusDays(1);
        LocalDateTime plusTwo = LocalDateTimeHelper.utcNow().plusDays(2);
        CreateEventCommand createEventCommand = new CreateEventCommand(
                "Event",
                "Desc",
                plusOne,
                plusTwo,
                "Zurich",
                "missing");
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> service.create(createEventCommand));

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

    @Test
    void shouldReturnEvents_When_SearchMatchesQuery() {
        // given
        String searchString = "Conference";
        Event matchingEvent = mock(Event.class);

        when(eventPersistencePort.searchByTitleOrDescription(searchString))
                .thenReturn(List.of(matchingEvent));

        // when
        List<Event> result = service.searchEvents(searchString);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertSame(matchingEvent, result.get(0));

        verify(eventPersistencePort, times(1)).searchByTitleOrDescription(searchString);
    }

    @Test
    void shouldReturnEmptyList_When_SearchFindsNoMatches() {
        // given
        String searchString = "NonExistentKeyword";

        when(eventPersistencePort.searchByTitleOrDescription(searchString))
                .thenReturn(Collections.emptyList());

        // when
        List<Event> result = service.searchEvents(searchString);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(eventPersistencePort, times(1)).searchByTitleOrDescription(searchString);
    }
}
