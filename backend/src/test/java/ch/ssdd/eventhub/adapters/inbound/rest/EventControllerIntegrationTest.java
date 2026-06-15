package ch.ssdd.eventhub.adapters.inbound.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.ssdd.eventhub.domain.Event;
import ch.ssdd.eventhub.domain.command.CreateEventCommand;
import ch.ssdd.eventhub.ports.inbound.CreateEventUseCase;
import ch.ssdd.eventhub.ports.inbound.DeleteEventUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadAllEventsUseCase;
import ch.ssdd.eventhub.ports.inbound.SearchEventsUseCase;
import ch.ssdd.eventhub.ports.inbound.UpdateEventUseCase;
import ch.ssdd.eventhub.security.config.InMemorySecurityConfiguration;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EventRestController.class)
@Import(InMemorySecurityConfiguration.class)
@ActiveProfiles("local")
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
    @MockitoBean
    SearchEventsUseCase searchEventsUseCase;

    @Test
    @WithMockUser(username = "alice_admin", authorities = {"Admin"})
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

        // The username is taken from the authenticated principal, not the (untrusted) request body.
        CreateEventCommand expectedPassedObject = new CreateEventCommand(
                "Concert",
                "Party time click",
                LocalDateTime.of(2026, Month.JUNE, 7, 20, 0),
                LocalDateTime.of(2026, Month.JUNE, 7, 23, 0),
                "Club ",
                "alice_admin"
        );
        verify(createEventUseCase).create(expectedPassedObject);
    }

    @Test
    @WithMockUser(username = "regular_user", authorities = {"User"})
    void should_AllowPublicAccess_When_GettingAllEvents() throws Exception {
        when(loadAllEventsUseCase.loadAllEvents()).thenReturn(List.of());

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "regular_user", authorities = {"User"})
    void should_AllowPublicAccessAndSanitize_When_SearchingEvents() throws Exception {
        // Assuming your SanitizedString or custom converter strips html tags during parameter binding
        when(searchEventsUseCase.searchEvents("concert")).thenReturn(List.of());

        mockMvc.perform(get("/api/events/search")
                        .param("query", "<script>alert(1)</script>concert"))
                .andExpect(status().isOk());

        verify(searchEventsUseCase).searchEvents("concert");
    }

    @Test
    @WithMockUser(username = "regular_user", authorities = {"User"})
    void should_ReturnForbidden_When_NonAdminAttemptsToCreateEvent() throws Exception {
        var payload = """
                {
                    "title": "Concert",
                    "description": "Party time",
                    "from": "2026-06-07T20:00:00",
                    "to": "2026-06-07T23:00:00",
                    "location": "Club",
                    "username": "user123"
                }
                """;

        mockMvc.perform(post("/api/events")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "alice_admin", authorities = {"Admin"})
    void should_AllowUpdate_When_UserIsAdmin() throws Exception {
        UUID id = UUID.randomUUID();
        var payload = """
                {
                    "title": "New Title",
                    "description": "New Description",
                    "from": "2026-06-07T20:00:00",
                    "to": "2026-06-07T23:00:00",
                    "location": "New Location"
                }
                """;

        Event mockEvent = mock(Event.class);
        when(updateEventUseCase.update(any(), any(), any(), any(), any(), any())).thenReturn(mockEvent);

        mockMvc.perform(put("/api/events/" + id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        verify(updateEventUseCase).update(
                id,
                "New Title",
                "New Description",
                LocalDateTime.of(2026, Month.JUNE, 7, 20, 0),
                LocalDateTime.of(2026, Month.JUNE, 7, 23, 0),
                "New Location"
        );
    }

    @Test
    @WithMockUser(username = "regular_user", authorities = {"User"})
    void should_ReturnForbidden_When_NonAdminAttemptsToUpdateEvent() throws Exception {
        UUID id = UUID.randomUUID();
        var payload = "{}";

        mockMvc.perform(put("/api/events/" + id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "alice_admin", authorities = {"Admin"})
    void should_AllowDelete_When_UserIsAdmin() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/events/" + id)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(deleteEventUseCase).deleteEvent(id);
    }

    @Test
    @WithMockUser(username = "regular_user", authorities = {"User"})
    void should_ReturnForbidden_When_NonAdminAttemptsToDeleteEvent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/events/" + id)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "regular_user", authorities = {"User"})
    void should_ReturnOkAndCallUseCase_When_SearchingEventsWithValidQuery() throws Exception {
        // Arrange
        String searchQuery = "TechConference";
        Event mockEvent = mock(Event.class);

        when(searchEventsUseCase.searchEvents(searchQuery)).thenReturn(List.of(mockEvent));

        // Act & Assert
        mockMvc.perform(get("/api/events/search")
                        .param("query", searchQuery)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(searchEventsUseCase).searchEvents(searchQuery);
    }

    @Test
    @WithMockUser(username = "regular_user", authorities = {"User"})
    void should_PassSanitizedQueryToUseCase_When_SearchQueryContainsUnsafeHtml() throws Exception {
        // Arrange
        String unsafeQuery = "<script>alert('XSS')</script>ZüriFest<iframe src='bad-url'></iframe>";
        String expectedCleanQuery = "ZüriFest"; // Expecting HTML/JS wrapper elements to be stripped out

        Event mockEvent = mock(Event.class);
        when(searchEventsUseCase.searchEvents(expectedCleanQuery)).thenReturn(List.of(mockEvent));

        // Act & Assert
        mockMvc.perform(get("/api/events/search")
                        .param("query", unsafeQuery)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verifies that the binding infrastructure converted the parameter into a cleaned string
        verify(searchEventsUseCase).searchEvents(expectedCleanQuery);
    }
}
