package ch.ssdd.eventhub.adapters.inbound.rest;

import ch.ssdd.eventhub.adapters.inbound.rest.dto.CreateEventRequest;
import ch.ssdd.eventhub.adapters.inbound.rest.dto.EventDTO;
import ch.ssdd.eventhub.domain.Event;
import ch.ssdd.eventhub.ports.inbound.CreateEventUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadEventUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventRestController {

    private final LoadEventUseCase loadEventUseCase;
    private final CreateEventUseCase createEventUseCase;

    public EventRestController(LoadEventUseCase loadEventUseCase, CreateEventUseCase createEventUseCase) {
        this.loadEventUseCase = loadEventUseCase;
        this.createEventUseCase = createEventUseCase;
    }

    @GetMapping
    public ResponseEntity<List<EventDTO>> get() {
        List<Event> events = loadEventUseCase.loadAllEvents();
        List<EventDTO> eventDTOs = events.stream().map(EventDTO::of).toList();
        return new ResponseEntity<>(eventDTOs, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<EventDTO> create(@RequestBody CreateEventRequest request) {

        Event event = createEventUseCase.create(
                request.title(),
                request.description(),
                request.from(),
                request.to(),
                request.location(),
                request.username()
        );
        return new ResponseEntity<>(EventDTO.of(event), HttpStatus.OK);
    }
}