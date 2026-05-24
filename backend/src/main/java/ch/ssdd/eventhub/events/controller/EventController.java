package ch.ssdd.eventhub.events.controller;

import ch.ssdd.eventhub.events.service.EventService;
import ch.ssdd.eventhub.events.dto.EventDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventDto> dummy() {
        return eventService.getEvents();
    }
}
