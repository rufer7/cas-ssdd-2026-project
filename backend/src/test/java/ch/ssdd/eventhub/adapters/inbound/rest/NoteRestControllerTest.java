package ch.ssdd.eventhub.adapters.inbound.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.ssdd.eventhub.adapters.inbound.rest.dto.CreateNoteRequestDto;
import ch.ssdd.eventhub.adapters.inbound.rest.dto.NoteResponseDto;
import ch.ssdd.eventhub.domain.Note;
import ch.ssdd.eventhub.domain.User;
import ch.ssdd.eventhub.ports.inbound.CreateNoteUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadNotesByUserUseCase;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

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
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("john");

        when(loadNotesByUserUseCase.loadNotesByUser("john"))
                .thenReturn(List.of(note1, note2));
        when(note1.createdBy()).thenReturn(dummyUser);
        when(note2.createdBy()).thenReturn(dummyUser);

        // when
        ResponseEntity<List<NoteResponseDto>> response = controller.getNotesByUser(authentication);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(loadNotesByUserUseCase, times(1)).loadNotesByUser("john");
    }

    @Test
    void shouldCreateNoteAsAuthenticatedPrincipal() {
        // given
        CreateNoteRequestDto request = new CreateNoteRequestDto("My note");
        Note note = mock(Note.class);
        User dummyUser = mock(User.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("john");

        when(createNoteUseCase.createNote(request.content(), "john"))
                .thenReturn(note);
        when(note.createdBy()).thenReturn(dummyUser);

        // when
        ResponseEntity<NoteResponseDto> response = controller.createNote(authentication, request);

        // then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        // the owner is the authenticated principal, not any client-supplied value
        verify(createNoteUseCase, times(1)).createNote(request.content(), "john");
    }
}
