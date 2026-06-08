package ch.ssdd.eventhub.adapters.inbound.rest;

import ch.ssdd.eventhub.adapters.inbound.rest.dto.CreateNoteRequestDto;
import ch.ssdd.eventhub.adapters.inbound.rest.dto.NoteResponseDto;
import ch.ssdd.eventhub.domain.Note;
import ch.ssdd.eventhub.domain.User;
import ch.ssdd.eventhub.ports.inbound.CreateNoteUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadNotesByUserUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteRestControllerTest {

    @Mock
    CreateNoteUseCase createNoteUseCase;

    @Mock
    LoadNotesByUserUseCase loadNotesByUserUseCase;

    @InjectMocks
    NoteRestController controller;

    @Test
    void shouldReturnNotesOfUser() {
        // given
        Note note1 = mock(Note.class);
        Note note2 = mock(Note.class);
        User dummyUser = mock(User.class);
        UserDetails principal = mock(UserDetails.class);
        when(principal.getUsername()).thenReturn("john");

        when(loadNotesByUserUseCase.loadNotesByUser("john"))
                .thenReturn(List.of(note1, note2));
        when(note1.createdBy()).thenReturn(dummyUser);
        when(note2.createdBy()).thenReturn(dummyUser);

        // when
        ResponseEntity<List<NoteResponseDto>> response = controller.getNotesByUser(principal);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(loadNotesByUserUseCase, times(1)).loadNotesByUser("john");
    }

    @Test
    void shouldCreateNote() {
        // given
        CreateNoteRequestDto request = new CreateNoteRequestDto("My note", "john");
        Note note = mock(Note.class);
        User dummyUser = mock(User.class);

        when(createNoteUseCase.createNote(request.content(), request.username()))
                .thenReturn(note);
        when(note.createdBy()).thenReturn(dummyUser);

        // when
        ResponseEntity<NoteResponseDto> response = controller.createNote(request);

        // then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(createNoteUseCase, times(1)).createNote(request.content(), request.username());
    }
}
