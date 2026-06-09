package ch.ssdd.eventhub.ports.inbound;

import ch.ssdd.eventhub.domain.Event;
import ch.ssdd.eventhub.domain.command.CreateEventCommand;

public interface CreateEventUseCase {

    Event create(CreateEventCommand createEventCommand);
}
