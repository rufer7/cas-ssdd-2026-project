package ch.ssdd.eventhub.events;

import ch.ssdd.eventhub.events.model.Event;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventsService {

    public List<Event> getEvents() {
        return List.of(new Event("Hello"), new Event("world"));
    }
}
