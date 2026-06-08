package ch.ssdd.eventhub.adapters.outbound.persistence;

import ch.ssdd.eventhub.common.LocalDateTimeHelper;
import ch.ssdd.eventhub.domain.Comment;
import ch.ssdd.eventhub.domain.Role;
import ch.ssdd.eventhub.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaCommentPersistenceAdapterTest {

    @Mock
    CommentRepository commentRepository;

    @Mock
    EventRepository eventRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    JpaCommentPersistenceAdapter adapter;

    private User buildUser() {
        return new User("john", "ext-1", Role.USER,
                LocalDateTimeHelper.utcNow().minusDays(1), LocalDateTimeHelper.utcNow().minusDays(1));
    }

    @Test
    void shouldSaveCommentSuccessfully() {
        // given
        UUID eventId = UUID.randomUUID();
        User user = buildUser();
        Comment comment = new Comment(
                "Great event!",
                user,
                LocalDateTimeHelper.utcNow(),
                user,
                LocalDateTimeHelper.utcNow());

        EventEntity eventEntity = mock(EventEntity.class);
        UserEntity userEntity = new UserEntity(user);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(eventEntity));
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(userEntity));
        when(commentRepository.save(any(CommentEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        Comment result = adapter.save(eventId, comment, user);

        // then
        assertNotNull(result);
        assertEquals("Great event!", result.content());

        ArgumentCaptor<CommentEntity> captor = ArgumentCaptor.forClass(CommentEntity.class);
        verify(commentRepository).save(captor.capture());
        assertEquals("Great event!", captor.getValue().getContent());
        assertEquals(eventEntity, captor.getValue().getEvent());
    }

    @Test
    void shouldThrowWhenEventNotFound() {
        // given
        UUID eventId = UUID.randomUUID();
        User user = buildUser();
        Comment comment = new Comment("Content", user, LocalDateTimeHelper.utcNow(), user, LocalDateTimeHelper.utcNow());

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        // when + then
        assertThrows(IllegalArgumentException.class, () -> adapter.save(eventId, comment, user));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        // given
        UUID eventId = UUID.randomUUID();
        User user = buildUser();
        Comment comment = new Comment("Content", user, LocalDateTimeHelper.utcNow(), user, LocalDateTimeHelper.utcNow());

        EventEntity eventEntity = mock(EventEntity.class);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(eventEntity));
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());

        // when + then
        assertThrows(IllegalArgumentException.class, () -> adapter.save(eventId, comment, user));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void shouldFindAllCommentsByEventId() {
        // given
        UUID eventId = UUID.randomUUID();
        CommentEntity e1 = mock(CommentEntity.class);
        CommentEntity e2 = mock(CommentEntity.class);
        Comment d1 = mock(Comment.class);
        Comment d2 = mock(Comment.class);

        when(e1.toComment()).thenReturn(d1);
        when(e2.toComment()).thenReturn(d2);
        when(commentRepository.findAllByEvent_Id(eventId)).thenReturn(List.of(e1, e2));

        // when
        List<Comment> result = adapter.findAllByEventId(eventId);

        // then
        assertEquals(2, result.size());
        assertSame(d1, result.get(0));
        assertSame(d2, result.get(1));

        verify(commentRepository).findAllByEvent_Id(eventId);
    }
}
