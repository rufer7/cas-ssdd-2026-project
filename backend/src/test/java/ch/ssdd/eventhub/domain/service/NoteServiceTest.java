package ch.ssdd.eventhub.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.ssdd.eventhub.common.LocalDateTimeHelper;
import ch.ssdd.eventhub.domain.Note;
import ch.ssdd.eventhub.domain.Role;
import ch.ssdd.eventhub.domain.User;
import ch.ssdd.eventhub.ports.outbound.NotePersistencePort;
import ch.ssdd.eventhub.ports.outbound.UserPersistencePort;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    NotePersistencePort notePersistencePort;

    @Mock
    UserPersistencePort userPersistencePort;

    @InjectMocks
    NoteService service;

    @Test
    void shouldCreateNoteSuccessfully() {
        // given
        User user = new User(
                "john",
                "ext-1",
                Role.USER,
                LocalDateTimeHelper.utcNow().minusDays(1),
                LocalDateTimeHelper.utcNow().minusDays(1));

        when(userPersistencePort.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(notePersistencePort.save(any(Note.class), eq(user)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Note result = service.createNote("My personal note", "john");

        // then
        assertNotNull(result);
        assertEquals("My personal note", result.content());
        assertEquals(user, result.createdBy());
        assertEquals(user, result.modifiedBy());

        verify(userPersistencePort, times(1)).findByUsername("john");
        verify(notePersistencePort, times(1)).save(any(Note.class), eq(user));
    }

    @Test
    void shouldProvisionUserWithLeastPrivilegeWhenNotFound() {
        // given: an authenticated principal with no local record yet
        when(userPersistencePort.findByUsername("newcomer"))
                .thenReturn(Optional.empty());
        when(userPersistencePort.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(notePersistencePort.save(any(Note.class), any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Note result = service.createNote("Content", "newcomer");

        // then: a USER-role record is provisioned (never escalated) and the note is saved
        assertNotNull(result);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userPersistencePort, times(1)).save(userCaptor.capture());
        assertEquals(Role.USER, userCaptor.getValue().role());
        verify(notePersistencePort, times(1)).save(any(Note.class), any(User.class));
    }

    @Test
    void shouldReturnEmptyNotesWhenUserNotProvisioned() {
        when(userPersistencePort.findByUsername("ghost")).thenReturn(Optional.empty());

        List<Note> result = service.loadNotesByUser("ghost");

        assertTrue(result.isEmpty());
        verify(notePersistencePort, never()).findAllByUser(any());
    }

    @Test
    void shouldLoadNotesOfUser() {
        // given
        User user = new User(
                "john",
                "ext-1",
                Role.USER,
                LocalDateTimeHelper.utcNow().minusDays(1),
                LocalDateTimeHelper.utcNow().minusDays(1));

        Note note1 = mock(Note.class);
        Note note2 = mock(Note.class);

        when(userPersistencePort.findByUsername("john"))
                .thenReturn(Optional.of(user));
        when(notePersistencePort.findAllByUser("john"))
                .thenReturn(List.of(note1, note2));

        // when
        List<Note> result = service.loadNotesByUser("john");

        // then
        assertEquals(2, result.size());
        assertSame(note1, result.get(0));
        assertSame(note2, result.get(1));

        verify(notePersistencePort, times(1)).findAllByUser("john");
    }
}
