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
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<EventResponseDto>> getAllEvents() {
        var eventDTOs = loadAllEventsUseCase.loadAllEvents()
                .stream()
                .map(EventResponseDto::of)
                .toList();
        return ResponseEntity.ok(eventDTOs);
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponseDto> createEvent(@RequestBody CreateEventRequestDto request) {

        var event = createEventUseCase.create(
                request.title(),
                request.description(),
                request.from(),
                request.to(),
                request.location(),
                request.username());
        return ResponseEntity.status(HttpStatus.CREATED).body(EventResponseDto.of(event));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<EventResponseDto> updateEvent(@PathVariable UUID id,
                                                        @RequestBody UpdateEventRequestDto request) {

        //updateEventUseCase.update(id, request.title(), request.description(), request.from(), request.to(), request.location());

        return ResponseEntity.of(EventResponseDto.of(null));
    }
    @DeleteMapping  ("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<EventResponseDto> deleteEvent(@PathVariable UUID id) {

        deleteEventUseCase.deleteEvent(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

        // if not successful ,return no found
    }
}