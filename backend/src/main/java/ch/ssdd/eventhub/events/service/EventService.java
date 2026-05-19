package ch.ssdd.eventhub.events.service;

import ch.ssdd.eventhub.events.dto.EventDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    public List<EventDto> getEvents() {
        return List.of(new EventDto("Hello"), new EventDto("world"));
    }
}
