package ch.ssdd.eventhub.adapters.inbound.rest;

import ch.ssdd.eventhub.adapters.inbound.rest.dto.CreateNoteRequestDto;
import ch.ssdd.eventhub.adapters.inbound.rest.dto.NoteResponseDto;
import ch.ssdd.eventhub.ports.inbound.CreateNoteUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadNotesByUserUseCase;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @PreAuthorize("hasAnyAuthority('Admin', 'User')")
    public ResponseEntity<List<NoteResponseDto>> getNotesByUser(Authentication authentication) {
        // Notes are scoped to the authenticated principal; a user can only ever see their own.
        var noteDtos = loadNotesByUserUseCase.loadNotesByUser(authentication.getName())
                .stream()
                .map(NoteResponseDto::of)
                .toList();
        return ResponseEntity.ok(noteDtos);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('Admin', 'User')")
    public ResponseEntity<NoteResponseDto> createNote(
            Authentication authentication,
            @RequestBody CreateNoteRequestDto request) {
        // The owner is the authenticated principal — never trust a client-supplied identity.
        var note = createNoteUseCase.createNote(request.content(), authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(NoteResponseDto.of(note));
    }
}
