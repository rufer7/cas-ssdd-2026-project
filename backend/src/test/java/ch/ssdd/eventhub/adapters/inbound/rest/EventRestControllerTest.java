package ch.ssdd.eventhub.adapters.inbound.rest;

import ch.ssdd.eventhub.adapters.inbound.rest.dto.CreateEventRequest;
import ch.ssdd.eventhub.adapters.inbound.rest.dto.EventDTO;
import ch.ssdd.eventhub.domain.Event;
import ch.ssdd.eventhub.ports.inbound.CreateEventUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadAllEventsUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        ResponseEntity<List<EventDTO>> response = controller.getAllEvents();

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(loadAllEventsUseCase, times(1)).loadAllEvents();
    }

    @Test
    void shouldCreateEvent() {
        // given
        CreateEventRequest request = new CreateEventRequest(
                "title",
                "desc",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                "Zurich",
                "john");

        var event = mock(Event.class);

        when(createEventUseCase.create(
                request.title(),
                request.description(),
                request.from(),
                request.to(),
                request.location(),
                request.username())).thenReturn(event);

        // when
        ResponseEntity<EventDTO> response = controller.createEvent(request);

        // then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(createEventUseCase, times(1)).create(
                request.title(),
                request.description(),
                request.from(),
                request.to(),
                request.location(),
                request.username());
    }
}
