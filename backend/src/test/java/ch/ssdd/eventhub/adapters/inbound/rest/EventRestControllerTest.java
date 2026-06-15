package ch.ssdd.eventhub.adapters.inbound.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.ssdd.eventhub.adapters.inbound.rest.config.SanitizedString;
import ch.ssdd.eventhub.adapters.inbound.rest.dto.CreateEventRequestDto;
import ch.ssdd.eventhub.adapters.inbound.rest.dto.EventResponseDto;
import ch.ssdd.eventhub.adapters.inbound.rest.dto.UpdateEventRequestDto;
import ch.ssdd.eventhub.common.LocalDateTimeHelper;
import ch.ssdd.eventhub.domain.Event;
import ch.ssdd.eventhub.domain.command.CreateEventCommand;
import ch.ssdd.eventhub.ports.inbound.CreateEventUseCase;
import ch.ssdd.eventhub.ports.inbound.DeleteEventUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadAllEventsUseCase;
import ch.ssdd.eventhub.ports.inbound.SearchEventsUseCase;
import ch.ssdd.eventhub.ports.inbound.UpdateEventUseCase;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class EventRestControllerTest {

    @Mock
    LoadAllEventsUseCase loadAllEventsUseCase;

    @Mock
    CreateEventUseCase createEventUseCase;

    @Mock
    UpdateEventUseCase updateEventUseCase;

    @Mock
    DeleteEventUseCase deleteEventUseCase;

    @Mock
    SearchEventsUseCase searchEventsUseCase;

    @InjectMocks
    EventRestController controller;

    @Test
    void shouldReturnAllEvents() {
        // given
        Event event1 = mock(Event.class);
        Event event2 = mock(Event.class);
        Authentication authentication = mock(Authentication.class);

        when(loadAllEventsUseCase.loadAllEvents())
                .thenReturn(List.of(event1, event2));

        // when
        ResponseEntity<List<EventResponseDto>> response = controller.getAllEvents(authentication);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(loadAllEventsUseCase, times(1)).loadAllEvents();
    }

    @Test
    void shouldSearchEvents() {
        // given
        SanitizedString searchString = new SanitizedString("concert");
        Event matchingEvent = mock(Event.class);

        when(searchEventsUseCase.searchEvents("concert"))
                .thenReturn(List.of(matchingEvent));

        // when
        ResponseEntity<List<EventResponseDto>> response = controller.searchEvents(searchString);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        verify(searchEventsUseCase, times(1)).searchEvents("concert");
    }

    @Test
    void shouldCreateEvent() {
        // given
        CreateEventRequestDto request = new CreateEventRequestDto(
                "title",
                "desc",
                LocalDateTimeHelper.utcNow().plusDays(1),
                LocalDateTimeHelper.utcNow().plusDays(2),
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

        // The controller derives the creator's username from the authenticated principal name,
        // so the principal here must resolve to "john" (it is ignored in the request body).
        var authentication = new UsernamePasswordAuthenticationToken("john", null, List.of());

        when(createEventUseCase.create(expectedCommand)).thenReturn(event);

        // when
        ResponseEntity<EventResponseDto> response = controller.createEvent(authentication, request);

        // then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(createEventUseCase, times(1)).create(expectedCommand);
    }

    @Test
    void shouldUploadFeaturedImage() throws IOException {
        var principal = mock(UserDetails.class);
        var authentication = new UsernamePasswordAuthenticationToken(principal, null, List.of());

        var eventId = UUID.randomUUID();

        var file = Files.readAllBytes(Path.of("src/test/resources/spring.png"));
        var multipartFile = new MockMultipartFile("file", "spring.png",
                "image/png", file);

        var response = controller.uploadFeaturedImage(authentication, eventId, multipartFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(updateEventUseCase, times(1)).updateFeaturedImage(eventId, file);
    }

    @Test
    void shouldUpdateEvent() {
        // given
        var eventId = UUID.randomUUID();
        var fromDate = LocalDateTimeHelper.utcNow().plusDays(1);
        var toDate = LocalDateTimeHelper.utcNow().plusDays(2);

        UpdateEventRequestDto request = new UpdateEventRequestDto(
                "Updated Title",
                "Updated Description",
                fromDate,
                toDate,
                "Bern"
        );

        var updatedEvent = mock(Event.class);

        var principal = mock(UserDetails.class);
        var authentication = new UsernamePasswordAuthenticationToken(principal, null, List.of());

        when(updateEventUseCase.update(eventId, request.title(), request.description(), request.from(), request.to(), request.location()))
                .thenReturn(updatedEvent);

        // when
        ResponseEntity<EventResponseDto> response = controller.updateEvent(authentication, eventId, request);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(updateEventUseCase, times(1)).update(eventId, request.title(), request.description(), request.from(), request.to(), request.location());
    }

    @Test
    void shouldDeleteEvent() {
        // given
        var eventId = UUID.randomUUID();

        var principal = mock(UserDetails.class);
        var authentication = new UsernamePasswordAuthenticationToken(principal, null, List.of());

        doNothing().when(deleteEventUseCase).deleteEvent(eventId);

        // when
        ResponseEntity<EventResponseDto> response = controller.deleteEvent(authentication, eventId);

        // then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(deleteEventUseCase, times(1)).deleteEvent(eventId);
    }
}
