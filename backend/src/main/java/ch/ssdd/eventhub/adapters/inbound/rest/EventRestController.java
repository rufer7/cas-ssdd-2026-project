package ch.ssdd.eventhub.adapters.inbound.rest;

import ch.ssdd.eventhub.adapters.inbound.rest.dto.CreateEventRequest;
import ch.ssdd.eventhub.domain.Event;
import ch.ssdd.eventhub.domain.Role;
import ch.ssdd.eventhub.domain.User;
import ch.ssdd.eventhub.ports.inbound.CreateEventUseCase;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/events")
public class EventRestController {

    private final CreateEventUseCase createEventUseCase;

    public EventRestController(CreateEventUseCase createEventUseCase) {
        this.createEventUseCase = createEventUseCase;
    }

    @PostMapping
    public Event create(@RequestBody CreateEventRequest request) {

        return createEventUseCase.create(
                request.title(),
                request.description(),
                request.from(),
                request.to(),
                request.location(),
                request.username()
        );
    }
}