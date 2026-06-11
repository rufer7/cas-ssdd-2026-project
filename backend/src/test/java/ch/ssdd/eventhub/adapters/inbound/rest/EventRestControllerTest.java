package ch.ssdd.eventhub.adapters.inbound.rest;

import ch.ssdd.eventhub.adapters.inbound.rest.dto.CreateEventRequestDto;
import ch.ssdd.eventhub.adapters.inbound.rest.dto.EventResponseDto;
import ch.ssdd.eventhub.common.LocalDateTimeHelper;
import ch.ssdd.eventhub.domain.Event;
import ch.ssdd.eventhub.domain.command.CreateEventCommand;
import ch.ssdd.eventhub.ports.inbound.CreateEventUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadAllEventsUseCase;
import ch.ssdd.eventhub.ports.inbound.UpdateEventUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

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

    @Mock
    UpdateEventUseCase updateEventUseCase;

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
        var principal = mock(UserDetails.class);

        when(createEventUseCase.create(expectedCommand)).thenReturn(event);

        ResponseEntity<EventResponseDto> response = controller.createEvent(principal, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(createEventUseCase, times(1)).create(expectedCommand);
    }

    @Test
    void shouldUploadFeaturedImage() throws IOException {
        var principal = mock(UserDetails.class);
        var eventId = UUID.randomUUID();
        var file = Files.readAllBytes(Path.of("src/test/resources/spring.png"));
        var multipartFile = new MockMultipartFile("file", "spring.png",
                "image/png", file);

        var response = controller.uploadFeaturedImage(principal, eventId, multipartFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(updateEventUseCase, times(1)).updateFeaturedImage(eventId, file);
    }
}
