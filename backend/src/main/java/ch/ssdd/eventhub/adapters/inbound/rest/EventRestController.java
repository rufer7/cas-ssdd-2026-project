package ch.ssdd.eventhub.adapters.inbound.rest;

import ch.ssdd.eventhub.adapters.inbound.rest.dto.CreateEventRequest;
import ch.ssdd.eventhub.adapters.inbound.rest.dto.EventDTO;
import ch.ssdd.eventhub.ports.inbound.CreateEventUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadAllEventsUseCase;
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

    private final LoadAllEventsUseCase loadAllEventsUseCase;
    private final CreateEventUseCase createEventUseCase;

    public EventRestController(LoadAllEventsUseCase loadAllEventsUseCase, CreateEventUseCase createEventUseCase) {
        this.loadAllEventsUseCase = loadAllEventsUseCase;
        this.createEventUseCase = createEventUseCase;
    }

    @GetMapping
    public ResponseEntity<List<EventDTO>> getAllEvents() {
        var eventDTOs = loadAllEventsUseCase.loadAllEvents()
                .stream()
                .map(EventDTO::of)
                .toList();
        return new ResponseEntity<>(eventDTOs, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<EventDTO> createEvent(@RequestBody CreateEventRequest request) {

        var event = createEventUseCase.create(
                request.title(),
                request.description(),
                request.from(),
                request.to(),
                request.location(),
                request.username());
        return new ResponseEntity<>(EventDTO.of(event), HttpStatus.CREATED);
    }
}