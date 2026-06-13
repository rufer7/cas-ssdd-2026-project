package ch.ssdd.eventhub.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.ssdd.eventhub.common.LocalDateTimeHelper;
import ch.ssdd.eventhub.domain.Event;
import ch.ssdd.eventhub.ports.outbound.EventPersistencePort;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventMutationServiceTest {

    @Mock
    private EventPersistencePort eventPersistencePort;

    @InjectMocks
    private EventMutationService eventMutationService;

    @Test
    void deleteEvent_ShouldCallPersistencePort() {
        UUID eventId = UUID.randomUUID();

        eventMutationService.deleteEvent(eventId);

        verify(eventPersistencePort).deleteById(eventId);
    }

    @Test
    void update_ShouldReturnSavedEvent_WhenEventExists() {
        UUID eventId = UUID.randomUUID();
        String title = "New Title";
        String description = "New Description";
        LocalDateTime from = LocalDateTimeHelper.utcNow();
        LocalDateTime to = LocalDateTimeHelper.utcNow().plusHours(2);
        String location = "Zurich";

        Event existingEvent = org.mockito.Mockito.mock(Event.class);
        Event updatedEvent = org.mockito.Mockito.mock(Event.class);
        Event savedEvent = org.mockito.Mockito.mock(Event.class);

        when(eventPersistencePort.findById(eventId)).thenReturn(Optional.of(existingEvent));
        when(existingEvent.updateDetails(title, description, from, to, location)).thenReturn(updatedEvent);
        when(eventPersistencePort.save(updatedEvent)).thenReturn(savedEvent);

        Event result = eventMutationService.update(eventId, title, description, from, to, location);

        assertEquals(savedEvent, result);
        verify(eventPersistencePort).findById(eventId);
        verify(existingEvent).updateDetails(title, description, from, to, location);
        verify(eventPersistencePort).save(updatedEvent);
    }

    @Test
    void update_ShouldThrowIllegalArgumentException_WhenEventDoesNotExist() {
        UUID eventId = UUID.randomUUID();
        String title = "Title";
        String description = "Description";
        LocalDateTime from = LocalDateTimeHelper.utcNow();
        LocalDateTime to = LocalDateTimeHelper.utcNow().plusHours(2);
        String location = "Zurich";

        when(eventPersistencePort.findById(eventId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                eventMutationService.update(eventId, title, description, from, to, location)
        );

        assertEquals("Event not found for id: " + eventId, exception.getMessage());
        verify(eventPersistencePort).findById(eventId);
    }
}
