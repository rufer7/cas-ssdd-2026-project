package ch.ssdd.eventhub.adapters.inbound.rest;

import ch.ssdd.eventhub.adapters.inbound.rest.dto.CreateNoteRequestDto;
import ch.ssdd.eventhub.adapters.inbound.rest.dto.NoteResponseDto;
import ch.ssdd.eventhub.ports.inbound.CreateNoteUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadAllNotesUseCase;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteRestController {

    private final CreateNoteUseCase createNoteUseCase;
    private final LoadAllNotesUseCase loadAllNotesUseCase;

    public NoteRestController(CreateNoteUseCase createNoteUseCase,
            LoadAllNotesUseCase loadAllNotesUseCase) {
        this.createNoteUseCase = createNoteUseCase;
        this.loadAllNotesUseCase = loadAllNotesUseCase;
    }

    @GetMapping
    public ResponseEntity<List<NoteResponseDto>> getAllNotes() {
        var noteDtos = loadAllNotesUseCase.loadAllNotes()
                .stream()
                .map(NoteResponseDto::of)
                .toList();
        return ResponseEntity.ok(noteDtos);
    }

    @PostMapping
    public ResponseEntity<NoteResponseDto> createNote(@RequestBody CreateNoteRequestDto request) {
        var note = createNoteUseCase.createNote(
                request.content(),
                request.username());
        return ResponseEntity.status(HttpStatus.CREATED).body(NoteResponseDto.of(note));
    }
}
