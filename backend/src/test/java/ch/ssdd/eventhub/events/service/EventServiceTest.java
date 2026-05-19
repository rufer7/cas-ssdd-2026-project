package ch.ssdd.eventhub.events.service;

import ch.ssdd.eventhub.events.dto.EventDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EventServiceTest {

    EventService classUndertest = new EventService();

    @Test
    void getEvents() {
        List<EventDto> eventDtos = classUndertest.getEvents();
        assertNotNull(eventDtos);
        assertFalse(eventDtos.isEmpty());

    }
}