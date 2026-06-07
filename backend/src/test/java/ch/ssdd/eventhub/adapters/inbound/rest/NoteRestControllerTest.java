package ch.ssdd.eventhub.adapters.inbound.rest;

import ch.ssdd.eventhub.adapters.inbound.rest.dto.CreateNoteRequestDto;
import ch.ssdd.eventhub.adapters.inbound.rest.dto.NoteResponseDto;
import ch.ssdd.eventhub.domain.Note;
import ch.ssdd.eventhub.ports.inbound.CreateNoteUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadAllNotesUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
    LoadAllNotesUseCase loadAllNotesUseCase;

    @InjectMocks
    NoteRestController controller;

    @Test
    void shouldReturnAllNotes() {
        // given
        Note note1 = mock(Note.class);
        Note note2 = mock(Note.class);

        when(loadAllNotesUseCase.loadAllNotes())
                .thenReturn(List.of(note1, note2));

        // when
        ResponseEntity<List<NoteResponseDto>> response = controller.getAllNotes();

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(loadAllNotesUseCase, times(1)).loadAllNotes();
    }

    @Test
    void shouldCreateNote() {
        // given
        CreateNoteRequestDto request = new CreateNoteRequestDto("My note", "john");
        Note note = mock(Note.class);

        when(createNoteUseCase.createNote(request.content(), request.username()))
                .thenReturn(note);

        // when
        ResponseEntity<NoteResponseDto> response = controller.createNote(request);

        // then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(createNoteUseCase, times(1)).createNote(request.content(), request.username());
    }
}
