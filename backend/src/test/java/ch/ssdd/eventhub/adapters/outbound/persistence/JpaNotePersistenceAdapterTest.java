package ch.ssdd.eventhub.adapters.outbound.persistence;

import ch.ssdd.eventhub.domain.Note;
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
class JpaNotePersistenceAdapterTest {

    @Mock
    NoteRepository noteRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    JpaNotePersistenceAdapter adapter;

    private User buildUser() {
        return new User("john", "ext-1", Role.USER,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1));
    }

    @Test
    void shouldSaveNoteSuccessfully() {
        // given
        User user = buildUser();
        Note note = new Note("My note", user, LocalDateTime.now(), user, LocalDateTime.now());
        UserEntity userEntity = new UserEntity(user);

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(userEntity));
        when(noteRepository.save(any(NoteEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        Note result = adapter.save(note, user);

        // then
        assertNotNull(result);
        assertEquals("My note", result.content());

        ArgumentCaptor<NoteEntity> captor = ArgumentCaptor.forClass(NoteEntity.class);
        verify(noteRepository).save(captor.capture());
        assertEquals("My note", captor.getValue().getContent());
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        // given
        User user = buildUser();
        Note note = new Note("Content", user, LocalDateTime.now(), user, LocalDateTime.now());

        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());

        // when + then
        assertThrows(IllegalArgumentException.class, () -> adapter.save(note, user));
        verify(noteRepository, never()).save(any());
    }

    @Test
    void shouldFindNotesOfUser() {
        // given
        var noteEntity1 = mock(NoteEntity.class);
        var noteEntity2 = mock(NoteEntity.class);
        var note1 = mock(Note.class);
        var note2 = mock(Note.class);
        var user = buildUser();
        var userEntity = new UserEntity(user);

        when(noteEntity1.toNote()).thenReturn(note1);
        when(noteEntity2.toNote()).thenReturn(note2);
        when(noteRepository.findByCreatedBy(userEntity)).thenReturn(List.of(noteEntity1, noteEntity2));
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(userEntity));

        // when
        List<Note> result = adapter.findAllByUser("john");

        // then
        assertEquals(2, result.size());
        assertSame(note1, result.get(0));
        assertSame(note2, result.get(1));

    }
}
