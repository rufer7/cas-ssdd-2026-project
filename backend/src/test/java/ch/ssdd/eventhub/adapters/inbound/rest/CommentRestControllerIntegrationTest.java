package ch.ssdd.eventhub.adapters.inbound.rest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.ssdd.eventhub.domain.Comment;
import ch.ssdd.eventhub.domain.User;
import ch.ssdd.eventhub.ports.inbound.AddCommentUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadCommentsByEventUseCase;
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

@WebMvcTest(CommentRestController.class)
@Import(InMemorySecurityConfiguration.class)
@ActiveProfiles("local")
class CommentRestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AddCommentUseCase addCommentUseCase;

    @MockitoBean
    private LoadCommentsByEventUseCase loadCommentsByEventUseCase;

    @Test
    @WithMockUser(username = "regular_user", authorities = {"User"})
    void should_AllowAccessAndReturnOk_When_UserHasUserRoleToGetComments() throws Exception {
        UUID eventId = UUID.randomUUID();
        when(loadCommentsByEventUseCase.loadCommentsByEvent(eventId)).thenReturn(List.of());

        mockMvc.perform(get("/api/events/" + eventId + "/comments"))
                .andExpect(status().isOk());

        verify(loadCommentsByEventUseCase).loadCommentsByEvent(eventId);
    }

    @Test
    @WithMockUser(username = "alice_admin", authorities = {"Admin"})
    void should_AllowAccessAndReturnOk_When_UserHasAdminRoleToGetComments() throws Exception {
        UUID eventId = UUID.randomUUID();
        when(loadCommentsByEventUseCase.loadCommentsByEvent(eventId)).thenReturn(List.of());

        mockMvc.perform(get("/api/events/" + eventId + "/comments"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "unauthorized_user", authorities = {"Guest"})
    void should_ReturnForbidden_When_UserLacksRequiredRoleToGetComments() throws Exception {
        UUID eventId = UUID.randomUUID();

        mockMvc.perform(get("/api/events/" + eventId + "/comments"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "bob_user", authorities = {"User"})
    void should_PassSanitizedDataAndAuthenticatedUserToUseCase_When_AddingCommentWithUnsafePayload() throws Exception {
        // Arrange
        UUID eventId = UUID.randomUUID();
        var unsafePayload = """
                {
                    "content": "This is a <script>alert('xss')</script>comment!"
                }
                """;
        String expectedCleanContent = "This is a comment!";

        User mockUser = mock(User.class);
        LocalDateTime localDateTime = LocalDateTime.of(2026, Month.JUNE, 7, 23, 0);

        Comment emptyComment = new Comment("a", mockUser, localDateTime, mockUser, localDateTime);
        when(addCommentUseCase.addComment(eventId, expectedCleanContent, "bob_user"))
                .thenReturn(emptyComment);

        // Act & Assert
        mockMvc.perform(post("/api/events/" + eventId + "/comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(unsafePayload))
                .andExpect(status().isCreated());

        verify(addCommentUseCase).addComment(eventId, expectedCleanContent, "bob_user");
    }

    @Test
    @WithMockUser(username = "unauthorized_user", authorities = {"Guest"})
    void should_ReturnForbidden_When_UserLacksRequiredRoleToAddComment() throws Exception {
        UUID eventId = UUID.randomUUID();
        var payload = """
                {
                    "content": "Valid comment content"
                }
                """;

        mockMvc.perform(post("/api/events/" + eventId + "/comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }
}
