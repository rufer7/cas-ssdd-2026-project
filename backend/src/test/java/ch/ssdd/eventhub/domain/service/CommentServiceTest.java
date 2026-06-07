package ch.ssdd.eventhub.domain.service;

import ch.ssdd.eventhub.domain.Comment;
import ch.ssdd.eventhub.domain.Role;
import ch.ssdd.eventhub.domain.User;
import ch.ssdd.eventhub.ports.outbound.CommentPersistencePort;
import ch.ssdd.eventhub.ports.outbound.UserPersistencePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    CommentPersistencePort commentPersistencePort;

    @Mock
    UserPersistencePort userPersistencePort;

    @InjectMocks
    CommentService service;

    @Test
    void shouldAddCommentSuccessfully() {
        // given
        User user = new User(
                "john",
                "ext-1",
                Role.USER,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1)
        );
        UUID eventId = UUID.randomUUID();

        when(userPersistencePort.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(commentPersistencePort.save(eq(eventId), any(Comment.class), eq(user)))
                .thenAnswer(invocation -> invocation.getArgument(1));

        // when
        Comment result = service.addComment(eventId, "Great event!", "john");

        // then
        assertNotNull(result);
        assertEquals("Great event!", result.content());
        assertEquals(user, result.createdBy());
        assertEquals(user, result.modifiedBy());

        verify(userPersistencePort, times(1)).findByUsername("john");
        verify(commentPersistencePort, times(1)).save(eq(eventId), any(Comment.class), eq(user));
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        // given
        UUID eventId = UUID.randomUUID();
        when(userPersistencePort.findByUsername("missing"))
                .thenReturn(Optional.empty());

        // when + then
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.addComment(eventId, "Content", "missing")
        );

        assertTrue(ex.getMessage().contains("User not found"));
        verify(commentPersistencePort, never()).save(any(), any(), any());
    }

    @Test
    void shouldLoadCommentsByEvent() {
        // given
        UUID eventId = UUID.randomUUID();
        Comment comment1 = mock(Comment.class);
        Comment comment2 = mock(Comment.class);

        when(commentPersistencePort.findAllByEventId(eventId))
                .thenReturn(List.of(comment1, comment2));

        // when
        List<Comment> result = service.loadCommentsByEvent(eventId);

        // then
        assertEquals(2, result.size());
        assertSame(comment1, result.get(0));
        assertSame(comment2, result.get(1));

        verify(commentPersistencePort, times(1)).findAllByEventId(eventId);
    }
}
