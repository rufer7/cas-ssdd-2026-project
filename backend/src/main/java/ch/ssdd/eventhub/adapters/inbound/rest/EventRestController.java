package ch.ssdd.eventhub.adapters.inbound.rest;

import ch.ssdd.eventhub.adapters.inbound.rest.dto.CreateEventRequestDto;
import ch.ssdd.eventhub.adapters.inbound.rest.dto.EventResponseDto;
import ch.ssdd.eventhub.adapters.inbound.rest.dto.UpdateEventRequestDto;
import ch.ssdd.eventhub.ports.inbound.CreateEventUseCase;
import ch.ssdd.eventhub.ports.inbound.DeleteEventUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadAllEventsUseCase;
import ch.ssdd.eventhub.ports.inbound.UpdateEventUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
public class EventRestController {

    private final LoadAllEventsUseCase loadAllEventsUseCase;
    private final CreateEventUseCase createEventUseCase;
    private final UpdateEventUseCase updateEventUseCase;
    private final DeleteEventUseCase deleteEventUseCase;

    public EventRestController(LoadAllEventsUseCase loadAllEventsUseCase, CreateEventUseCase createEventUseCase, UpdateEventUseCase updateEventUseCase, DeleteEventUseCase deleteEventUseCase) {
        this.loadAllEventsUseCase = loadAllEventsUseCase;
        this.createEventUseCase = createEventUseCase;
        this.updateEventUseCase = updateEventUseCase;
        this.deleteEventUseCase = deleteEventUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('Admin', 'User')")
    public ResponseEntity<List<EventResponseDto>> getAllEvents(Authentication authentication) {
        var eventDtos = loadAllEventsUseCase.loadAllEvents()
                .stream()
                .map(EventResponseDto::of)
                .toList();
        return ResponseEntity.ok(eventDtos);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<EventResponseDto> createEvent(Authentication authentication,
                                                        @RequestBody CreateEventRequestDto request) {

        var event = createEventUseCase.create(request.toCommand(authentication.getName()));

        return ResponseEntity.status(HttpStatus.CREATED).body(EventResponseDto.of(event));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<EventResponseDto> updateEvent(Authentication authentication,
                                                        @PathVariable UUID id,
                                                        @RequestBody UpdateEventRequestDto request) {

        var updatedEvent = updateEventUseCase.update(id, request.title(), request.description(), request.from(), request.to(), request.location());

        return ResponseEntity.ok(EventResponseDto.of(updatedEvent));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<EventResponseDto> deleteEvent(Authentication authentication,
                                                        @PathVariable UUID id) {
        deleteEventUseCase.deleteEvent(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
