package ch.ssdd.eventhub.ports.inbound;

import ch.ssdd.eventhub.domain.Event;

import java.util.List;

public interface LoadEventUseCase {

    List<Event> loadAllEvents();
}