package ch.ssdd.eventhub.ports.inbound;

import ch.ssdd.eventhub.domain.Event;

import java.util.List;

public interface SearchEventsUseCase {

    List<Event> searchEvents(String searchString);
}