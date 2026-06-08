package ch.ssdd.eventhub.adapters.inbound.rest;

import ch.ssdd.eventhub.adapters.inbound.rest.dto.CreateNoteRequestDto;
import ch.ssdd.eventhub.adapters.inbound.rest.dto.NoteResponseDto;
import ch.ssdd.eventhub.ports.inbound.CreateNoteUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadNotesByUserUseCase;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final LoadNotesByUserUseCase loadNotesByUserUseCase;

    public NoteRestController(CreateNoteUseCase createNoteUseCase,
            LoadNotesByUserUseCase loadNotesByUserUseCase) {
        this.createNoteUseCase = createNoteUseCase;
        this.loadNotesByUserUseCase = loadNotesByUserUseCase;
    }

    @GetMapping
    public ResponseEntity<List<NoteResponseDto>> getNotesByUser(@AuthenticationPrincipal UserDetails principal) {
        var noteDtos = loadNotesByUserUseCase.loadNotesByUser(principal.getUsername())
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
