package ch.ssdd.eventhub.adapters.inbound.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.ssdd.eventhub.domain.Note;
import ch.ssdd.eventhub.domain.User;
import ch.ssdd.eventhub.ports.inbound.CreateNoteUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadNotesByUserUseCase;
import ch.ssdd.eventhub.security.config.InMemorySecurityConfiguration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NoteRestController.class)
@Import(InMemorySecurityConfiguration.class)
@ActiveProfiles("local")
class NoteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    CreateNoteUseCase createNoteUseCase;
    @MockitoBean
    LoadNotesByUserUseCase loadNotesByUserUseCase;

    @Test
    @WithMockUser(username = "alice_admin", authorities = {"Admin"})
    void should_ReturnOkAndOwnNotes_When_AdminRequestsNotes() throws Exception {
        Note mockNote = mock(Note.class);
        User user = mock(User.class);
        when(mockNote.createdBy()).thenReturn(user);
        when(mockNote.modifiedBy()).thenReturn(user);
        when(loadNotesByUserUseCase.loadNotesByUser("alice_admin")).thenReturn(List.of(mockNote));

        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isOk());

        verify(loadNotesByUserUseCase).loadNotesByUser("alice_admin");
    }

    @Test
    @WithMockUser(username = "regular_user", authorities = {"User"})
    void should_ReturnOkAndOwnNotes_When_UserRequestsNotes() throws Exception {
        Note mockNote = mock(Note.class);
        User user = mock(User.class);
        when(mockNote.createdBy()).thenReturn(user);
        when(mockNote.modifiedBy()).thenReturn(user);
        when(loadNotesByUserUseCase.loadNotesByUser("regular_user")).thenReturn(List.of(mockNote));

        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isOk());

        verify(loadNotesByUserUseCase).loadNotesByUser("regular_user");
    }

    @Test
    void should_ReturnUnauthorized_When_UnauthenticatedUserRequestsNotes() throws Exception {
        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "alice_admin", authorities = {"Admin"})
    void should_CreateNoteForPrincipal_When_AdminPostsNote() throws Exception {
        var payload = """
                {
                    "content": "My important note"
                }
                """;


        Note mockNote = mock(Note.class);
        User user = mock(User.class);
        when(mockNote.createdBy()).thenReturn(user);
        when(mockNote.modifiedBy()).thenReturn(user);
        when(createNoteUseCase.createNote(any(), any())).thenReturn(mockNote);

        mockMvc.perform(post("/api/notes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        verify(createNoteUseCase).createNote("My important note", "alice_admin");
    }

    @Test
    @WithMockUser(username = "regular_user", authorities = {"User"})
    void should_CreateNoteForPrincipal_When_UserPostsNote() throws Exception {
        var payload = """
                {
                    "content": "User's personal note"
                }
                """;

        Note mockNote = mock(Note.class);
        User user = mock(User.class);
        when(mockNote.createdBy()).thenReturn(user);
        when(mockNote.modifiedBy()).thenReturn(user);
        when(createNoteUseCase.createNote(any(), any())).thenReturn(mockNote);

        mockMvc.perform(post("/api/notes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        verify(createNoteUseCase).createNote("User&#39;s personal note", "regular_user");
    }

    @Test
    void should_ReturnUnauthorized_When_UnauthenticatedUserAttemptsToCreateNote() throws Exception {
        var payload = """
                {
                    "content": "Sneaky note"
                }
                """;

        mockMvc.perform(post("/api/notes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "alice_admin", authorities = {"Admin"})
    void should_ScopeNotesToPrincipal_When_TwoUsersHaveNotes() throws Exception {
        // alice_admin must only ever receive her own notes, not those of other users
        when(loadNotesByUserUseCase.loadNotesByUser("alice_admin")).thenReturn(List.of());
        when(loadNotesByUserUseCase.loadNotesByUser("other_user")).thenReturn(List.of(mock(Note.class)));

        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isOk());

        verify(loadNotesByUserUseCase).loadNotesByUser("alice_admin");
    }

    @Test
    @WithMockUser(username = "alice_admin", authorities = {"Admin"})
    void should_PassSanitizedContent_When_NoteContainsUnsafeHtml() throws Exception {
        var unsafePayload = """
                {
                    "content": "<script>evil()</script>Reminder to buy milk<iframe src='hack'></iframe>"
                }
                """;

        Note mockNote = mock(Note.class);
        User user = mock(User.class);
        when(mockNote.createdBy()).thenReturn(user);
        when(mockNote.modifiedBy()).thenReturn(user);
        when(createNoteUseCase.createNote(any(), any())).thenReturn(mockNote);

        mockMvc.perform(post("/api/notes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(unsafePayload))
                .andExpect(status().isCreated());

        verify(createNoteUseCase).createNote("Reminder to buy milk", "alice_admin");
    }
}