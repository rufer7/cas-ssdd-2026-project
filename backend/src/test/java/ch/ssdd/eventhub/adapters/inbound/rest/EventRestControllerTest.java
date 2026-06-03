package ch.ssdd.eventhub.adapters.inbound.rest;

import ch.ssdd.eventhub.adapters.inbound.rest.dto.CreateEventRequestDto;
import ch.ssdd.eventhub.adapters.inbound.rest.dto.EventResponseDto;
import ch.ssdd.eventhub.domain.Event;
import ch.ssdd.eventhub.domain.command.CreateEventCommand;
import ch.ssdd.eventhub.ports.inbound.CreateEventUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadAllEventsUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventRestControllerTest {

    @Mock
    LoadAllEventsUseCase loadAllEventsUseCase;

    @Mock
    CreateEventUseCase createEventUseCase;

    @InjectMocks
    EventRestController controller;

    @Test
    void shouldReturnAllEvents() {
        // given
        Event event1 = mock(Event.class);
        Event event2 = mock(Event.class);

        when(loadAllEventsUseCase.loadAllEvents())
                .thenReturn(List.of(event1, event2));

        // when
        ResponseEntity<List<EventResponseDto>> response = controller.getAllEvents();

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(loadAllEventsUseCase, times(1)).loadAllEvents();
    }

    @Test
    void shouldCreateEvent() {
        CreateEventRequestDto request = new CreateEventRequestDto(
                "title",
                "desc",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                "Zurich",
                "john"
        );

        var expectedCommand = new CreateEventCommand(
                request.title(),
                request.description(),
                request.from(),
                request.to(),
                request.location(),
                "john"
        );

        var event = mock(Event.class);
        UserDetails principal = mock(UserDetails.class);
        when(principal.getUsername()).thenReturn("john");

        when(createEventUseCase.create(expectedCommand)).thenReturn(event);

        ResponseEntity<EventResponseDto> response = controller.createEvent(principal, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(createEventUseCase, times(1)).create(expectedCommand);
    }
}
