package ch.ssdd.eventhub.adapters.inbound.rest;

import ch.ssdd.eventhub.domain.Event;
import ch.ssdd.eventhub.domain.command.CreateEventCommand;
import ch.ssdd.eventhub.ports.inbound.CreateEventUseCase;
import ch.ssdd.eventhub.ports.inbound.DeleteEventUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadAllEventsUseCase;
import ch.ssdd.eventhub.ports.inbound.UpdateEventUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.Month;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventRestController.class)
class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    CreateEventUseCase createEventUseCase;
    @MockitoBean
    LoadAllEventsUseCase loadAllEventsUseCase;
    @MockitoBean
    UpdateEventUseCase updateEventUseCase;
    @MockitoBean
    DeleteEventUseCase deleteEventUseCase;


    @Test
    @WithMockUser(username = "alice_admin", roles = {"ADMIN"})
    void should_PassSanitizedDataToUseCase_When_ControllerReceivesUnsafePayload() throws Exception {
        // Arrange
        var unsafePayload = """
                {
                    "title": "<script>evil()</script>Concert",
                    "description": "Party time <a href='javascript:exploit()'>click</a>",
                    "from": "2026-06-07T20:00:00",
                    "to": "2026-06-07T23:00:00",
                    "location": "Club <iframe src='hack'></iframe>",
                    "username": "user123"
                }
                """;


        Event mockEvent = mock(Event.class);
        when(createEventUseCase.create(any())).thenReturn(mockEvent);

        // Act & Assert
        mockMvc.perform(post("/api/events")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(unsafePayload))
                .andExpect(status().isCreated());

        CreateEventCommand expectedPassedObject = new CreateEventCommand(
                "Concert",                        // <script> completely removed
                "Party time click",                    // javascript link stripped out
                LocalDateTime.of(2026, Month.JUNE, 7, 20, 0),   // Dates pass perfectly
                LocalDateTime.of(2026, Month.JUNE, 7, 23, 0),
                "Club ",                       // <iframe> completely removed
                "user123"                              // Clean strings passed normally
        );
        verify(createEventUseCase).create(expectedPassedObject);
    }
}
